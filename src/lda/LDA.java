package lda;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by found on 1/15/16.
 */
public class LDA {
    int[] wArray = null;
    int[] dArray = null;
    int[] zArray = null;
    double[][] theta = null;
    double[][] phi= null;
    int[][] n_k_w = null;
    int[][] n_d_k = null;
    double[] sumK = null;
    int nIter = 0;
    double alpha = 0;
    double beta = 0;
    int V,K,D;
	Random rd = new Random(System.currentTimeMillis());

    public LDA(int nIter,double alpha,double beta,int K){
        this.nIter = nIter;
        this.alpha = alpha;
        this.beta = beta;
        this.K = K;
    }

    /**
     * fit model to wArray and dArray
     * @param wArray all words of all doc wArray[i] is word of doc darray[i]
     * @param dArray
     */
    public void fit(int[] wArray,int[] dArray){
        V = wArray[array_max(wArray)]+1;
        D = dArray[array_max(dArray)]+1;
		zArray = new int[wArray.length];
		theta = new double[D][K];
		phi = new double[K][V];
		sumK = new double[K];
		for(int k=0;k<K;k++){
			sumK[k] = V*beta;
		}
		n_k_w = new int[K][V];
		n_d_k = new int[D][K];

		//initial main matrix
		for(int i =0;i<zArray.length;i++){
			zArray[i] = rd.nextInt(K);
			n_k_w[zArray[i]][wArray[i]]++;
			n_d_k[dArray[i]][zArray[i]]++;
			sumK[zArray[i]]++;
		}
		//start burnning
		long t0 = System.currentTimeMillis();
		for(int sp=0;sp<nIter;sp++)
		{
			for(int i=0;i<wArray.length;i++){
				int w = wArray[i];
				int z = zArray[i];
				int d = dArray[i];
				n_k_w[z][w]--;
				n_d_k[d][z]--;
				sumK[z]--;
				int newz = sampleFrom(calp(i));
				n_k_w[newz][w]++;
				n_d_k[d][newz]++;
				sumK[z]++;
			}
			if(sp%10 ==0 ){
				// TODO:use logger
				System.out.println("sampling "+sp+
						" log likehood is:"+likehood());
				long t1 = System.currentTimeMillis();
				System.out.println("using time:"+(t1-t0)/1000.0);
				t0 = t1;
			}
		}

    }

	protected double likehood(){
		return 0.0;
	}
	public int sampleFrom(double[] q){
		for(int i=1;i<q.length;i++){
			q[i] += q[i-1];
		}
		assert Math.abs(q[q.length-1] -1) < 0.0001;
		double rd_d = rd.nextDouble();
		for(int i=0;i<q.length;i++){
			if(rd_d > q[i]) return i;
		}
		return -1;
	}
	protected double[] calp(int i){
		double[] q = new double[K];
		double qsum = 0.0;
		for(int k=0;k<K;k++){
			q[k] = (n_d_k[dArray[i]][k]+alpha) * (n_k_w[k][wArray[i]]+beta) / sumK[k];
			qsum += q[k];
		}
		for(int k=0;k<K;k++){
			q[k] /= qsum;
		}
		return q;
	}
    public double[][] getParam(String paramName){
        // TODO:waiting
        return null;
    }

    public static int array_max(Object array){
        if(array instanceof int[]){
            int maxi = 0;
            int[] iarray = (int[]) array;
            for(int i=0;i<iarray.length;i++){
                maxi = iarray[i]>iarray[maxi]?i:maxi;
            }
            return maxi;
        }else if(array instanceof double[]){
            int maxi = 0;
            int[] iarray = (int[]) array;
            for(int i=0;i<iarray.length;i++){
                maxi = iarray[i]>iarray[maxi]?i:maxi;
            }
            return maxi;
        }else if(array instanceof float[]){
            int maxi = 0;
            int[] iarray = (int[]) array;
            for(int i=0;i<iarray.length;i++){
                maxi = iarray[i]>iarray[maxi]?i:maxi;
            }
            return maxi;
        }else{
            return -1;
        }
    }
	public static void main(String[] arg){
		int D = 1000;
		int V = 500;
		int K = 20;
		int Dlen = 200;
		LDA model = new LDA(2000,1.0,0.01,K);
		int[] wArray = new int[D*Dlen];
		int[] dArray = new int[D*Dlen];
		for(int i = 0;i < D*Dlen;i ++)
		{
			wArray[i] = model.rd.nextInt(V);
			dArray[i] = model.rd.nextInt(D);
		}

		model.fit(wArray,dArray);
	}
}
