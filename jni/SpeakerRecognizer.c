#include "edu_ucla_nesl_speech_RecordService.h"
#include "lib_mfcc/mfcc_bare.h"
#include <android/log.h>
#include <string.h>
#include <math.h>
#include <time.h>
#include <stdio.h>

#define APPNAME "SpeakerRecognizer"

typedef struct by_speaker {
	char name[10];
	double m_m[108];
	double m_v[108];
	double m_w[9];
} by_speaker;
const int num_mixture = 9;
const int num_speaker = 5;
by_speaker speaker_gmm[5];

double gaussmixp(const double y[12], const double m[108], const double v[108], const double w[9]) {
	int i = 0, j = 0;
	double mi[12], vi[12], x[12], py[num_mixture];
	double lvm = 0.0;
	double dot = 0.0;
	double pic = -0.5 * 12 * log(2 * 3.14);
	double sumv = 0.0;
	double maxpy = 0.0;
	for (i = 0; i < num_mixture; i++) {
		lvm = 0.0; sumv = 0.0;
		for (j = 0; j < 12; j++) {
			mi[j] = -m[j * num_mixture];
			vi[j] = v[j * num_mixture];
			sumv += vi[j];
			vi[j] = 1.0 / vi[j];
		}
		lvm = log(w[i]) - 0.5 * log(sumv);
		for (j = 0; j < 12; j++) {
			x[j] = pow((y[j] + mi[j]), 2);
		}
		dot = 0;
		for (j = 0; j < 12; j++) {
			dot += x[j] * vi[j];
		}
		//printf("%lf\n", dot);
		py[i] = lvm - 0.5 * dot;
		if (py[i] > maxpy) {
			maxpy = py[i];
		}
	}
	double sumpy = 0.0;
	for (i = 0; i < num_mixture; i++) {
		sumpy += exp(py[i] - maxpy);
	}
	return log(sumpy) + maxpy + pic;
}

void load_speaker_gmm () {
	printf("In load_speaker_gmm\n");
	memset(speaker_gmm, 0, sizeof(speaker_gmm));
	char buffer[100];
	memset(buffer, 0, sizeof(buffer));
	FILE *fin = fopen("/sdcard/gmm/by_speaker.txt", "r");
	fgets(buffer, sizeof(buffer), fin);

	int i = 0;
	for (i = 0; i < num_speaker; i++) {
		// get name
		memset(buffer, 0, sizeof(buffer));
		fgets(buffer, sizeof(buffer), fin);
		strncpy(speaker_gmm[i].name, buffer, strlen(buffer) - 1);
		//printf("%d: %s ", i, speaker_gmm[i].name);


		// get M
		memset(buffer, 0, sizeof(buffer));
		fgets(buffer, sizeof(buffer), fin);
		if (strncmp(buffer, "M\n", 2) == 0) {
			//printf("read M!\n");
			int j = 0;
			for (j = 0; j < 108; j++) {
				fscanf(fin, "%lf\n", &speaker_gmm[i].m_m[j]);
			}
		}

		// get V
		memset(buffer, 0, sizeof(buffer));
		fgets(buffer, sizeof(buffer), fin);
		if (strncmp(buffer, "V\n", 2) == 0) {
			//printf("read V!\n");
			int j = 0;
			for (j = 0; j < 108; j++) {
				fscanf(fin, "%lf\n", &speaker_gmm[i].m_v[j]);
			}
		}

		// get W
		memset(buffer, 0, sizeof(buffer));
		fgets(buffer, sizeof(buffer), fin);
		if (strncmp(buffer, "W\n", 2) == 0) {
			//printf("read W!\n");
			int j = 0;
			for (j = 0; j < 9; j++) {
				fscanf(fin, "%lf\n", &speaker_gmm[i].m_w[j]);
			}
		}
	}
	printf("Finish reading gmms.\n");
	fclose(fin);
}

void loadReal(const char* filename, int ndata, real_T* data) {
	FILE* f = fopen(filename, "r");
	//  printf("Loading %d values from %s\n\n", ndata, filename);
	int ii;
	for (ii = 0; ii < ndata; ++ii)
	fscanf(f, "%lf", &data[ii]);
	fclose(f);
}

void loadComplexReal(const char* filename, int ndata, creal_T* data) {
	FILE* f = fopen(filename, "r");
	//  printf("Loading %d values from %s\n\n", ndata, filename);
	int ii;
	for (ii = 0; ii < ndata; ++ii) {
	fscanf(f, "%lf", &(data[ii].re));
	fscanf(f, "%lf", &(data[ii].im));
	}
	fclose(f);
}

JNIEXPORT jint JNICALL Java_edu_ucla_nesl_speech_RecordService_getSpeaker
  (JNIEnv *env, jobject obj, jshortArray signal, jint size, jint inc) {
  	jshort *values = (*env)->GetShortArrayElements(env, signal, NULL);
 //  	int k = 0;
 //  	for (k = 0; k < size; k++) {
 //  		__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "value[%d] = %lf\n", k, 1.0 * (short)values[k]);
	// }
	double d_frame[128];
	real_T d_hamming[128];
	real_T d_mel_filters[2016];
	creal_T d_dct_coeff[32];
	creal_T d_mel_e[13];
	real_T d_mel[12];
	real_T fftA = 2.0;
	real_T fftB = 64.0;

	loadReal("/sdcard/mfcc/real_hamming_coeff.txt", 128, d_hamming);
	loadReal("/sdcard/mfcc/real_mel_filterbank.txt", 2016, d_mel_filters);
	loadComplexReal("/sdcard/mfcc/creal_dct_coeff.txt", 32, d_dct_coeff);

	double logprob[num_speaker];
	memset(logprob, 0, sizeof(logprob));
	load_speaker_gmm();

	int i = 0, j = 0;
	while (i < size) {
		/* get mfcc for each frame */
		for (j = 0; j < 128; j++) {
			d_frame[j] = 1.0 * (int)values[i + j];
			
		}
		mfcc_bare(d_frame, d_hamming, d_mel_filters, fftA, fftB, d_dct_coeff, d_mel_e);
		for (j = 0; j < 12; j++) {
			d_mel[j] = d_mel_e[j].re;
		}
		/* gmm prob for each frame per each speaker */
		for (j = 0; j < num_speaker; j++) {
			by_speaker gmm = speaker_gmm[j];
			double result = gaussmixp(d_mel, gmm.m_m, gmm.m_v, gmm.m_w);
			logprob[j] += result;
		}
		i += inc;
	}

	double max_prob = -10000000.0;
	int max_index = -1;
	for (j = 0; j < num_speaker; j++) {
		//printf("result=%lf\n", logprob[j]);
		__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "for speaker %d, prob=%lf\n", (j + 1), logprob[j]);
		if (logprob[j] > max_prob) {
			max_prob = logprob[j];
			max_index = j;
		}
	}
	__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "speaker label = %d\n", (max_index + 1));
	return (max_index + 1);
}