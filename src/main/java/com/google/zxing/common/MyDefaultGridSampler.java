package main.java.com.google.zxing.common;

import main.java.com.google.zxing.NotFoundException;

public class MyDefaultGridSampler extends MyGridSampler {
	
	  @Override
	  public BitVectorMatrix sampleGrid(BitVectorMatrix image,
	                              int dimensionX,
	                              int dimensionY,
	                              float p1ToX, float p1ToY,
	                              float p2ToX, float p2ToY,
	                              float p3ToX, float p3ToY,
	                              float p4ToX, float p4ToY,
	                              float p1FromX, float p1FromY,
	                              float p2FromX, float p2FromY,
	                              float p3FromX, float p3FromY,
	                              float p4FromX, float p4FromY) throws NotFoundException {

	    PerspectiveTransform transform = PerspectiveTransform.quadrilateralToQuadrilateral(
	        p1ToX, p1ToY, p2ToX, p2ToY, p3ToX, p3ToY, p4ToX, p4ToY,
	        p1FromX, p1FromY, p2FromX, p2FromY, p3FromX, p3FromY, p4FromX, p4FromY);

	    return sampleGrid(image, dimensionX, dimensionY, transform);
	  }

	  @Override
	  public BitVectorMatrix sampleGrid(BitVectorMatrix image,
	                              int dimensionX,
	                              int dimensionY,
	                              PerspectiveTransform transform) throws NotFoundException {
	    if (dimensionX <= 0 || dimensionY <= 0) {
	      throw NotFoundException.getNotFoundInstance();      
	    }
	    BitVectorMatrix bits = new BitVectorMatrix(dimensionX, dimensionY);
	    float[] points = new float[2 * dimensionX];
	    for (int y = 0; y < dimensionY; y++) {
	      int max = points.length;
	      float iValue = (float) y + 0.5f;
	      for (int x = 0; x < max; x += 2) {
	        points[x] = (float) (x / 2) + 0.5f;
	        points[x + 1] = iValue;
	      }
	      transform.transformPoints(points);
	      // Quick check to see if points transformed to something inside the image;
	      // sufficient to check the endpoints
	      checkAndNudgePoints(image, points);
	      try {
	        for (int x = 0; x < max; x += 2) {
	          if (image.get((int) points[x], (int) points[x + 1])[0] && image.get((int) points[x], (int) points[x + 1])[1]) {
	            // Black(-ish) pixel
	            bits.set(x / 2, y, 0);
	            bits.set(x / 2, y, 1);
	          }
	          else if (image.get((int) points[x], (int) points[x + 1])[0] && !image.get((int) points[x], (int) points[x + 1])[1])
	        	  bits.set(x / 2, y, 0);
	          else if (!image.get((int) points[x], (int) points[x + 1])[0] && image.get((int) points[x], (int) points[x + 1])[1])
	        	  bits.set(x / 2, y, 1);
	        }
	      } catch (ArrayIndexOutOfBoundsException aioobe) {
	        // This feels wrong, but, sometimes if the finder patterns are misidentified, the resulting
	        // transform gets "twisted" such that it maps a straight line of points to a set of points
	        // whose endpoints are in bounds, but others are not. There is probably some mathematical
	        // way to detect this about the transformation that I don't know yet.
	        // This results in an ugly runtime exception despite our clever checks above -- can't have
	        // that. We could check each point's coordinates but that feels duplicative. We settle for
	        // catching and wrapping ArrayIndexOutOfBoundsException.
	        throw NotFoundException.getNotFoundInstance();
	      }
	    }
	    return bits;
	  }

}
