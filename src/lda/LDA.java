package lda;

import java.util.ArrayList;
import java.util.Random;
import org.apache.commons.math3.special.Gamma;
import java.util.Arrays;
import org.apache.commons.math3.linear.*;

/**
 * Created by found on 1/15/16.
 */
public class LDA {
    int[] wArray = null;
    int[] dArray = null;
    int[] zArray = null;
    double[][] theta = null;
    double[][] phi= null;
    double[][] n_k_w = null;
    double[][] n_d_k = null;
	double[] n_k = null;
	double[] n_d = null;
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
     * @param wArray all words of all doc wArray[i] is word of doc dArray[i]
     * @param dArray dArray[i] is the number of doc that wArray[i] occur in
     */
    public void fit(int[] wArray,int[] dArray){
		this.wArray = wArray;
		this.dArray = dArray;
        V = wArray[array_max(wArray)]+1;
        D = dArray[array_max(dArray)]+1;
		zArray = new int[wArray.length];
		theta = new double[D][K];
		phi = new double[K][V];
		sumK = new double[K];
		this.n_k = new double[K];
		this.n_d = new double[D];
		for(int k=0;k<K;k++){
			sumK[k] = V*beta;
		}
		n_k_w = new double[K][V];
		n_d_k = new double[D][K];

		//initial main matrix
		for(int i =0;i<zArray.length;i++){
			zArray[i] = rd.nextInt(K);
			n_k_w[zArray[i]][wArray[i]]++;
			n_d_k[dArray[i]][zArray[i]]++;
			sumK[zArray[i]]++;
			n_k[zArray[i]]++;
			n_d[dArray[i]]++;
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
				n_k[z]--;
				n_d[d]--;
				int newz = sampleFrom(calp(i));
				n_k_w[newz][w]++;
				n_d_k[d][newz]++;
				sumK[z]++;
				n_k[z]++;
				n_d[d]++;
			}
			if(sp%1 ==0 ){
				// TODO:use logger
				System.out.println("sampling "+sp+
						" log likehood is:"+likehood());
				long t1 = System.currentTimeMillis();
				System.out.println("using time:"+(t1-t0)/1000.0);
				t0 = t1;
			}
		}

    }

	/**
	 * cal log likehood of p(z,w)
	 * 	= Σ(Δ(n_k + beta)/Δ(beta))+Σ(Δ(n_M+alfa)/Δ(alfa))
	 * 	can reference to:https://github.com/ariddell/lda/blob/develop/lda/_lda.pyx
	 * @return
     */
	protected double likehood(){
		double ll = 0.;
		double lgamma_beta = Gamma.logGamma(beta);
		double lgamma_alpha = Gamma.logGamma(this.alpha);
		//cal log p(w|z)
		ll += K * Gamma.logGamma(beta*V);
		for(int k=0;k<K;k++){
			ll -= Gamma.logGamma(n_k[k]+beta*V);
			for(int w=0;w<V;w++){
				if(n_k_w[k][w] > 0)
					ll +=( Gamma.logGamma(beta+n_k_w[k][w]) - lgamma_beta);
			}

		}
		System.err.print("t");
		//cal log p(z)
		for(int d=0;d<D;d++){
			ll += (Gamma.logGamma(alpha * K) -
					Gamma.logGamma(alpha * K + n_d[d]));
			for(int k=0;k<K;k++) {
				if (n_d_k[d][k]>0)
					ll += Gamma.logGamma(alpha + n_d_k[d][k])-lgamma_alpha;
			}
		}
		return ll;
	}

	/**
	 * cal delta function(alpha), Δ(α)=ΣΓ(α_i) / Γ(Σα_i)
	 * @param alpha
	 * @return
     */
	protected double delta(double[] alpha){
		double result = 0.0;
		for(double a_i:alpha){
			result += Gamma.gamma(a_i);
		}
		result /= Gamma.gamma(array_sum(alpha));
		return result;
	}
	public int sampleFrom(double[] q){
		for(int i=1;i<q.length;i++){
			q[i] += q[i-1];
		}
		assert Math.abs(q[q.length-1] -1) < 0.0001;
		double rd_d = rd.nextDouble();
		for(int i=0;i<q.length;i++){
			if(rd_d <= q[i]) return i;
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

	public static double array_sum(Object array){
		if(array instanceof int[]){
			double sum = 0.;
			int[] ar = (int[]) array;
			for(int i:ar)
				sum += i;
			return sum;
		}else if(array instanceof double[]){
			double sum = 0.;
			double[] ar = (double[]) array;
			for(double i:ar)
				sum += i;
			return sum;
		}else
			return Double.NaN;

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
	public static void test(){
		int D = 100;
		int V = 100;
		int K = 10;
		int Dlen = 20;
		LDA model = new LDA(2000000,1.0,0.01,K);
		int[] wArray = new int[D*Dlen];
		int[] dArray = new int[D*Dlen];
		for(int i = 0;i < D*Dlen;i ++)
		{
			wArray[i] = model.rd.nextInt(V);
			dArray[i] = model.rd.nextInt(D);
		}

		model.fit(wArray,dArray);
	}
	public static void test_fit(){
		int D = 4;
		int V = 2;
		int K = 2;
		int Dlen = 4;
		LDA model = new LDA(10,1.0,0.1,K);
		int[] wArray = {
				0, 0, 0, 1,
				0, 0, 0, 1,
				1, 1, 1, 0,
				1, 1, 1, 0
		};
		int[] dArray = {
				0, 0, 0, 0,
				1, 1, 1, 1,
				2, 2, 2, 2,
				3, 3, 3, 3
		};
		model.fit(wArray,dArray);
	}
	public static void main(String[] a){
		test_fit();
	}
}
