package lda;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import org.apache.commons.math3.special.Gamma;
import org.apache.commons.math3.linear.*;

/**
 * Created by found on 1/15/16.
 */
public class LDA {
	int MAX_STEADY = 100;
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
		RealMatrix theta_mat = new Array2DRowRealMatrix(D,K);
		phi = new double[K][V];
		RealMatrix phi_mat = new Array2DRowRealMatrix(K,V);
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
		boolean judgeSteady = false;
		boolean isSteady = false;
		double steadyCount = 0;
		double lastTenLikehood = 0.0;
		int lastAdded = 0;
		double thisTenLikehood = 0.0;
		int thisAdded = 0;
		double previousLh = Double.NEGATIVE_INFINITY;
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
				zArray[i] = newz;
				n_k_w[newz][w]++;
				n_d_k[d][newz]++;
				sumK[z]++;
				n_k[z]++;
				n_d[d]++;
			}
			if((sp%10 ==0 || judgeSteady) && !isSteady) {
				if (judgeSteady) {
					if(lastAdded<10){
						lastTenLikehood += likehood();
						lastAdded++;
					}else if(thisAdded < 10){
						thisTenLikehood += likehood();
						thisAdded ++;
					}else{
						lastTenLikehood /= 10.0;
						thisTenLikehood /= 10.0;
						if(this.diff(lastTenLikehood,thisTenLikehood)<0.001||
								thisTenLikehood<lastTenLikehood){
							System.out.println(lastTenLikehood+" new:"+thisTenLikehood);
							isSteady = true;
							continue;
						}
						// TODO:use logger
						System.out.println("not converged:"+lastTenLikehood+" new:"+thisTenLikehood);
						lastTenLikehood = thisTenLikehood * 10;
						thisAdded = 0;
						thisTenLikehood = 0.0;
					}
				} else {
					// TODO:use logger
					double lh = likehood();
					System.out.println("sampling " + sp +
							"  likehood is:" + lh);
					long t1 = System.currentTimeMillis();
					System.out.println("using time:" + (t1 - t0) / 1000.0);
					t0 = t1;
					if (this.diff(previousLh,lh)<0.001 || lh < previousLh) {
						judgeSteady = true;
					}
					previousLh = lh;
				}
			}
			if(isSteady){
				if(steadyCount < MAX_STEADY){
					theta_mat = theta_mat.add(new Array2DRowRealMatrix(n_d_k));
					phi_mat = phi_mat.add(new Array2DRowRealMatrix(n_k_w));
					if(steadyCount % 10 ==0){
						// TODO:use logger instead
						System.out.println("steady process:sampling "+steadyCount);
					}
					steadyCount++;
				}else{
					break;
				}
			}
		}//burning over
		//start cal theta and phi
		// TODO:use logger instead
		System.out.println("starting cal theta");
		theta_mat = theta_mat.scalarMultiply(1.0/steadyCount);
		phi_mat  = phi_mat.scalarMultiply(1.0/steadyCount);
		theta_mat = theta_mat.scalarAdd(alpha);
		phi_mat = phi_mat.scalarAdd(beta);
		//phi_mat.sum(1). Get row sum of mat
		RealVector theta_sum = theta_mat.operate(new ArrayRealVector(K,1.0));
		RealVector phi_sum = phi_mat.operate(new ArrayRealVector(V,1.0));
		for(int i=0;i<theta_sum.getDimension();i++)
			theta_sum.setEntry(i,1.0/theta_sum.getEntry(i));
		for(int i = 0;i<phi_sum.getDimension();i++)
			phi_sum.setEntry(i,1.0/phi_sum.getEntry(i));
		theta_mat = new DiagonalMatrix(theta_sum.toArray()).multiply(theta_mat);
		phi_mat = new DiagonalMatrix(phi_sum.toArray()).multiply(phi_mat);
		this.theta = theta_mat.getData();
		this.phi = phi_mat.getData();
    }

	/**
	 * convert X into int[2][],int[0] is wArray,int[1] is dArray[]
	 * @param X X[d][i] represent word i in document d
	 * @return int[0] is wArray,int[1] is dArray[]
     */
	public static int[][] convert2Arrays(int[][] X){
		ArrayList<Integer> wArrayList = new ArrayList<>();
		ArrayList<Integer> dArrayList = new ArrayList<>();
		for(int d = 0;d<X.length;d++)
			for(int w=0;w<X[0].length;w++)
				for(int count=0;count<X[d][w];count++)
				{
					wArrayList.add(w);
					dArrayList.add(d);
				}
		int[][] result = new int[2][];
		result[0] = new int[wArrayList.size()];
		result[1] = new int[dArrayList.size()];
		for(int i=0;i<wArrayList.size();i++)
			result[0][i] = wArrayList.get(i);
		for(int i=0;i<dArrayList.size();i++)
			result[1][i] = dArrayList.get(i);
		return result;
	}
	/**
	 * transfer X to wArray and dArray ,and fit(wArray,dArray)
	 * @param X X[d][i] represent how many times word i occured in document d
     */
	public void fit(int[][] X){
		int[][] wdArray = convert2Arrays(X);
		fit(wdArray[0],wdArray[1]);
	}

	/**
	 * cal log likehood of p(z,w)
	 * 	= Σ(Δ(n_k + beta)/Δ(beta))+Σ(Δ(n_M+alfa)/Δ(alfa))
	 * 	can reference to:https://github.com/ariddell/lda/blob/develop/lda/_lda.pyx
	 * @return
     */
	protected  double diff(double d1,double d2){
		return Math.abs( (d2-d1) / d1 );
	}
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

	protected double log_likehood(){
		double ll = 0.;
		Array2DRowRealMatrix n_k_w_mat =
				new Array2DRowRealMatrix(n_k_w);
		Array2DRowRealMatrix n_m_k_mat =
				new Array2DRowRealMatrix(n_d_k);
		ArrayRealVector beta_v = new ArrayRealVector(V,beta);
		ArrayRealVector alpha_v = new ArrayRealVector(K,alpha);
		for(int i=0;i<K;i++){
			RealVector n_k_beta = n_k_w_mat.getRowVector(i).add(beta_v);
			System.out.println("n_k+beta:"+n_k_beta);
			ll += ( log_delta(n_k_beta.toArray()) - log_delta(beta_v.toArray()) );
		}
		double ll0 = ll;
		System.out.println("p(w|z):"+ll);
		for(int d=0;d < D;d++){
			RealVector n_d_alpha = n_m_k_mat.getRowVector(d).add(alpha_v);
			System.out.println("n_m+al:"+n_d_alpha);
			ll += ( log_delta(n_d_alpha.toArray()) - log_delta(alpha_v.toArray()));
		}
		System.out.println("p(z):"+(ll-ll0));
		return  ll;
	}
	/**
	 * cal delta function(alpha), Δ(α)=ΣΓ(α_i) / Γ(Σα_i)
	 * @param alpha
	 * @return
     */
	protected double log_delta(double[] alpha){
		double result = 0.0;
		for(int i=0;i<alpha.length;i++){
			result += Gamma.logGamma(alpha[i]);
		}
		double sum_a = 0.;
		for(double a_i:alpha)
			sum_a += a_i;
		result -= Gamma.logGamma(sum_a);
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
		return paramName.endsWith("theta")?this.theta:this.phi;
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
		int D = 1000;
		int V = 500;
		int K = 50;
		int Dlen = 100;
		LDA model = new LDA(2000000,1.0,0.01,K);
		int[] wArray = new int[D*Dlen];
		int[] dArray = new int[D*Dlen];
		for(int i = 0;i < D*Dlen;i ++)
		{
			wArray[i] = model.rd.nextInt(V);
			dArray[i] = model.rd.nextInt(D);
		}

		model.fit(wArray,dArray);
		for(double[] phi_k:model.phi)
			System.out.println(Arrays.deepToString(model.phi));
		for(double[] theta_k:model.theta)
			System.out.println(Arrays.deepToString(model.theta));
	}
	public static void test_fit(){
		int D = 2;
		int V = 2;
		int K = 2;
		int Dlen = 5;
		LDA model = new LDA(500,1.0,0.1,K);
		int[] wArray = {
				 0, 0,0,0, 1,
				 1, 1,1, 1,0
		};
		int[] dArray = {
				0, 0, 0,0,0,
				1, 1, 1,1,1
		};
		model.fit(wArray,dArray);
		System.out.print("\nphi:\n");
			System.out.println(Arrays.deepToString(model.phi));
		System.out.print("theta:\n");
			System.out.println(Arrays.deepToString(model.theta));
	}
	public static void main(String[] a){
		test_fit();
	}
}
