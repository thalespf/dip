Digital Image Processing
=========

Some plays in digital image processing.

###RestoreImageSliced
  Reconstruct a image that was sliced in various pieces out of order. The method used for similarity is based on the shortest distance between the pixels on a window.

###Deblurring
  A class to blur and deblur images with motion blur. The blur method (convolution) use a PSF (point-spread function) defined in Gonzalez.
  The PSF (in domain space) is transformed in fequency domain and is represented by a OTF that make linear motion blur. The deblur use the Wiener method to make the image deconvolution.
  Obs.: This code is only for tests and needed design.
