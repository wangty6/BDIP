package dip;

import java.awt.image.BufferedImage;
import java.io.Console;

public class Processor {
	static public void print(int[][] x) {
		for (int i = 0; i < x.length; i ++) {
			for (int j = 0; j < x[i].length; j ++)
				System.out.print(Integer.toString(x[i][j]) + ' ');
			System.out.println();
		}
	}
	static public BufferedImage scale(BufferedImage img, int M, int N) {
		int n = img.getHeight(), m = img.getWidth();
		int[][] b = new int[m][n];
		for (int i = 0; i < m; i ++)
			for (int j = 0; j < n; j ++) {
				b[i][j] = img.getRGB(i, j) & 0xff;
			}

		int[][] c = new int[M][N];
		double mf = 1. * M / m;
		double nf = 1. * N / n;
		for (int i = 0; i < M; i ++)
			for (int j = 0; j < N; j ++) {
				double x = i / mf, y = j / nf;
				int x1 = (int) Math.floor(x);
				int x2 = x1 + 1;
				if (x2 >= m) continue;
				int y1 = (int) Math.floor(y);
				int y2 = y1 + 1;
				if (y2 >= n) continue;
				double R1 = (x2 - x)/(x2 - x1)*b[x1][y1] + (x - x1)/(x2 - x1)*b[x2][y1];
				double R2 = (x2 - x)/(x2 - x1)*b[x1][y2] + (x - x1)/(x2 - x1)*b[x2][y2];
				c[i][j] = (int) Math.round((y2 - y)/(y2 - y1)*R1 + (y - y1)/(y2 - y1)*R2);
				//if (c[i][j] < 0 || c[i][j] > 255) {
				//	System.out.format("%d %d %f %f %f %f %d %d %d\n", i, j, R1, R2, x, y, x1, y1, b[x1][y1]);
				//}
			}
		
		//print(c);
		
		BufferedImage res = new BufferedImage(M, N, BufferedImage.TYPE_BYTE_GRAY);
		for (int i = 0; i < M; i ++)
			for (int j = 0; j < N; j ++) {
				res.setRGB(i, j, (c[i][j] << 16) | (c[i][j] << 8) | c[i][j]);
			}
		return res;
	}
	
	static public BufferedImage quantize(BufferedImage img, int dep) {
		int n = img.getHeight(), m = img.getWidth();
		int[][] b = new int[m][n];
		for (int i = 0; i < m; i ++)
			for (int j = 0; j < n; j ++) {
				b[i][j] = img.getRGB(i, j) & 0xff;
			}
		
		int[][] c = new int[m][n];
		for (int i = 0; i < m; i ++)
			for (int j = 0; j < n; j ++) {
				double frac = 1. * b[i][j] / 255;
				for (int k = 1; k <= dep; k ++)
					if (frac < 1. / dep * k + 1e-9) {
						c[i][j] = (int)Math.round((k - 1) * (1. / (dep - 1) * 255));
						break;
					}
			}
		
		print(c);
		
		BufferedImage res = new BufferedImage(m, n, BufferedImage.TYPE_BYTE_GRAY);
		for (int i = 0; i < m; i ++)
			for (int j = 0; j < n; j ++) {
				res.setRGB(i, j, (c[i][j] << 16) | (c[i][j] << 8) | c[i][j]);
			}
		return res;
	}
	static public BufferedImage equalize(BufferedImage img) {
		int n = img.getHeight(), m = img.getWidth();
		int[][] b = new int[m][n];
		for (int i = 0; i < m; i ++)
			for (int j = 0; j < n; j ++) {
				b[i][j] = img.getRGB(i, j) & 0xff;
			}
		int[] cdf = new int[256];
		int[] h = new int[256];
		int mn = 0x3f3f3f3f, mx = 0;
		for (int i = 0; i < m; i ++)
			for (int j = 0; j < n; j ++) {
				cdf[b[i][j]] += 1;
				mn = Integer.min(mn, b[i][j]);
				mx = Integer.max(mx, b[i][j]);
			}

		for (int i = 1; i < 256; i ++)
			cdf[i] += cdf[i - 1];

		for (int i = 0; i < 256; i ++) {
			h[i] = (int)Math.round(1.0 * (cdf[i] - cdf[mn]) / (m * n - cdf[mn]) * 255);
		}


		for (int i = 0; i < 256; i ++) cdf[i] = 0;
		for (int i = 0; i < m; i ++)
			for (int j = 0; j < n; j ++) {
				b[i][j] = h[b[i][j]];
				cdf[b[i][j]] += 1;
			}
		for (int i = 1; i < 256; i ++) cdf[i] += cdf[i - 1];
		for (int i = 0; i < 256; i ++) {
			System.out.println("" + i + "	" + cdf[i]);
		}
		BufferedImage res = new BufferedImage(m, n, BufferedImage.TYPE_BYTE_GRAY);
		for (int i = 0; i < m; i ++)
			for (int j = 0; j < n; j ++) {
				res.setRGB(i, j, (b[i][j] << 16) | (b[i][j] << 8) | b[i][j]);
			}
		return res;
	}
	static public BufferedImage conv(BufferedImage img, int fm, int fn, int[][] f) {
		int n = img.getHeight(), m = img.getWidth();
		int[][] b = new int[m][n];
		for (int i = 0; i < m; i ++)
			for (int j = 0; j < n; j ++) {
				b[i][j] = img.getRGB(i, j) & 0xff;
			}
		int[][] c = new int[m][n];
		int p = fm / 2;
		int q = fn / 2;
		int mod = 0;
		for (int i = 0; i < fm; i ++)
			for (int j = 0; j < fn; j ++)
				mod += Math.abs(f[i][j]);
		for (int i = 0; i < m; i ++)
			for (int j = 0; j < n; j ++) {
				int sum = 0;
				for (int s = -p; s <= p; s ++)
					for (int t = -q; t <= q; t ++) {
						int x = i - s;
						int y = j - t;
						if (x < 0 || y < 0 || x >= m || y >= n) continue;
						sum += b[x][y] * f[p + s][q + t];
					}
				c[i][j] = Integer.min(255, Integer.max(0, (int) Math.round(1.0 * sum)));
			}
		BufferedImage res = new BufferedImage(m, n, BufferedImage.TYPE_BYTE_GRAY);
		for (int i = 0; i < m; i ++)
			for (int j = 0; j < n; j ++) {
				res.setRGB(i, j, (c[i][j] << 16) | (c[i][j] << 8) | c[i][j]);
			}
		return res;
	}
}
