package assg1;

public class Statistics {
	public double covariance(int[] X, int[] Y) {
		int XY[] = new int[X.length];
		for (int i = 0; i < XY.length; i++)
			XY[i] = X[i] * Y[i];
		return (mean(XY) - mean(X) * mean(Y));
	}

	public double SpearmanStatistic(int[] X, int[] Y) {
		double xmean = mean(X);
		double ymean = mean(Y);
		double num = 0;
		double den1 = 0;
		double den2 = 0;
		for (int i = 0; i < X.length; i++) {
			num += ((X[i] - xmean) * (Y[i] - ymean));
			den1 += ((X[i] - xmean) * (X[i] - xmean));
			den2 += ((Y[i] - ymean) * (Y[i] - ymean));
		}
		return (num / Math.sqrt(den1 * den2));
	}

	public double PearsonStatistic(int[] X, int[] Y) {
		return (covariance(X, Y) / Math.sqrt(covariance(X, X)
				* covariance(Y, Y)));
	}

	public static double mean(int[] X) {
		double sum = 0;
		for (int i = 0; i < X.length; i++)
			sum += X[i];
		return (sum / X.length);
	}

	public static int median(int[] array, int left, int right) {
		int k = array.length / 2;
		while (true) {
			if (right <= left + 1) {
				if (right == left + 1 && array[right] < array[left])
					swap(array, left, right);
				return array[k];
			} else {
				int middle = (left + right) >>> 1;
				swap(array, middle, left + 1);
				if (array[left] > array[right])
					swap(array, left, right);
				if (array[left + 1] > array[right])
					swap(array, left + 1, right);
				if (array[left] > array[left + 1])
					swap(array, left, left + 1);
				int i = left + 1;
				int j = right;
				int pivot = array[left + 1];
				while (true) {
					do
						i++;
					while (array[i] < pivot);
					do
						j--;
					while (array[j] > pivot);
					if (j < i)
						break;
					swap(array, i, j);
				}
				array[left + 1] = array[j];
				array[j] = pivot;
				if (j >= k)
					right = j - 1;
				if (j <= k)
					left = i;
			}
		}
	}

	public static void swap(int[] array, int a, int b) {
		int temp = array[a];
		array[a] = array[b];
		array[b] = temp;
	}
}