package assg1;

public class Statistics
{
	public static double covariance(double[] X, double[] Y)
	{
		double XY[] = new double[X.length];
		for (int i=0; i<XY.length; i++)
			XY[i] = X[i] * Y[i];
		return (mean(XY) - mean(X)*mean(Y));
	}
	
	public static double mean(double[] X)
	{
		double sum = 0;
		for (int i=0; i<X.length; i++)
			sum+=X[i];
		return (sum/X.length);
	}
	private static double median(double[] array, int left, int right)
	{
		int k = array.length/2;
		while(true)
		{
			if(right <= left+1)
			{ 
				if(right==left+1 && array[right]<array[left])
					swap( array, left, right );
				return array[k];
			} 
			else
			{
				int middle = ( left + right ) >>> 1; 
				swap( array, middle, left + 1 );
				if( array[ left ] > array[ right ] )
					swap( array, left, right );
				if( array[ left + 1 ] > array[ right ] )
					swap( array, left + 1, right );
				if( array[ left ] > array[ left + 1 ] )
					swap( array, left, left + 1 );
				int i = left + 1;
				int j = right;
				double pivot = array[ left + 1 ];
				while( true )
				{ 
					do
						i++; 
					while( array[ i ] < pivot ); 
					do
						j--;
					while( array[ j ] > pivot ); 
					if( j < i )
						break;
					swap( array, i, j );
				}
				array[ left + 1 ] = array[ j ];
				array[ j ] = pivot;
				if( j >= k )
					right = j - 1;
				if( j <= k )
					left = i;
			}
		}
	}
	private static void swap( double[] array, int a, int b )
	{
		double temp = array[ a ];
		array[ a ] = array[ b ];
		array[ b ] = temp;
	}
}