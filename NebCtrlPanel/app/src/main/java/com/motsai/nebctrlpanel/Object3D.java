/*
    Copyright 2016 Arnaud Guyon

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
package com.motsai.nebctrlpanel;

import android.opengl.Matrix;


import fr.arnaudguyon.smartgl.opengl.RenderObject;

public class Object3D extends RenderObject {

	private float mPosX, mPosY, mPosZ;
	private float mRotX, mRotY, mRotZ;
	private float mScaleX, mScaleY, mScaleZ;
	//private float mQw, mQx, mQy, mQz;
	private float[] mRotM;

	public Object3D() {
		super(true);
		mPosX = mPosY = mPosZ = 0;
		mScaleX = mScaleY = mScaleZ = 1;
		mRotX = mRotY = mRotZ = 0;
		//mQw = mQx = mQy = mQz = 0;
		mRotM = new float[16];
		setRotateQuaternionM(mRotM, 0, 0, 0, 0, 0);
	}

	final public void setPos(float x, float y, float z) {
		mPosX = x;
		mPosY = y;
		mPosZ = z;
        invalidMatrix();
	}

	final public float getPosX() {
		return mPosX;
	}

	final public float getPosY() {
		return mPosY;
	}

	final public float getPosZ() {
		return mPosZ;
	}

	final public void setScale(float x, float y, float z) {
		mScaleX = x;
		mScaleY = y;
		mScaleZ = z;
        invalidMatrix();
	}

	final public float getScaleX() {
		return mScaleX;
	}

	final public float getScaleY() {
		return mScaleY;
	}

	final public float getScaleZ() {
		return mScaleZ;
	}

	final public void setRotation(float x, float y, float z) {
		mRotX = (x % 360f);
		mRotY = (y % 360f);
		mRotZ = (z % 360f);
        invalidMatrix();
	}

	final public float getRotX() {
		return mRotX;
	}

	final public float getRotY() {
		return mRotY;
	}

	final public float getRotZ() {
		return mRotZ;
	}
	
	final public void addRotX(float dx) {
		mRotX += dx;
        invalidMatrix();
	}
	
	final public void addRotY(float dy) {
		mRotY += dy;
        invalidMatrix();
	}

	final public void addRotZ(float dz) {
		mRotZ += dz;
        invalidMatrix();
	}

	public static void setRotateQuaternionM(float[] m, int mOffset, float w, float x, float y, float z) {
		if (m.length < mOffset + 16) {
			throw new IllegalArgumentException("m.length < mOffset + 16");
		}
		final float xx = x*x, yy = y*y, zz = z*z;
		final float xy = x*y, yz = y*z, xz = x*z;
		final float xw = x*w, yw = y*w, zw = z*w;
		m[mOffset +  0] = 1 - 2*yy - 2*zz;
		m[mOffset +  1] = 2*xy + 2*zw;
		m[mOffset +  2] = 2*xz - 2*yw;
		m[mOffset +  3] = 0;

		m[mOffset +  4] = 2*xy - 2*zw;
		m[mOffset +  5] = 1 - 2*xx - 2*zz;
		m[mOffset +  6] = 2*yz + 2*xw;
		m[mOffset +  7] = 0;

		m[mOffset +  8] = 2*xz + 2*yw;
		m[mOffset +  9] = 2*yz - 2*xw;
		m[mOffset + 10] = 1 - 2*xx - 2*yy;
		m[mOffset + 11] = 0;

		m[mOffset + 12] = 0;
		m[mOffset + 13] = 0;
		m[mOffset + 14] = 0;
		m[mOffset + 15] = 1;
	}

	public void setQuaternion(float w, float x, float y, float z) {
	//	mQw = w;
	//	mQx = x;
	//	mQy = y;
	//	mQz = z;
		setRotateQuaternionM(mRotM, 0, w, x, y, z);
		invalidMatrix();
	}

	@Override
	final public void computeMatrix(float[] matrix) {

		Matrix.setIdentityM(matrix, 0);
		Matrix.translateM(matrix, 0, mPosX, mPosY, mPosZ);
		//QMatrix.rotateQuaternionM(matrix, 0, mQw, mQx, mQy, mQz);
		Matrix.multiplyMM(matrix, 0, matrix, 0, mRotM, 0);
		if ((mScaleX != 1) || (mScaleY != 1) || (mScaleZ != 1)) {
			Matrix.scaleM(matrix, 0, mScaleX, mScaleY, mScaleZ);
		}
	}

	@Override
	public void releaseResources() {
		super.releaseResources();
	}

	public static class QMatrix extends Matrix {
		private final static float[] sTemp1 = new float[32];
		/**
		 +     * Rotates the given matrix in place by the given quaternion rotation
		 +	 *
		 +	 * @param m The float array that holds matrix to rotate
		 +	 * @param mOffset The offset into <code>m</code> where the matrix starts
		 +	 * @param w The w-component of the quaternion
		 +	 * @param x The x-component of the quaternion
		 +	 * @param y The y-component of the quaternion
		 +	 * @param z The z-component of the quaternion
		 +	 */
 		public static void rotateQuaternionM(float[] m, int mOffset, float w, float x, float y, float z) {
			synchronized (sTemp1) {
				setRotateQuaternionM(sTemp1, 0, w, x, y, z);
				multiplyMM(sTemp1, 16, m, mOffset, sTemp1, 0);
				System.arraycopy(sTemp1, 16, m, mOffset, 16);
			}
 		}

		/**
		 * Rotates the given matrix by the given quaternion rotation, putting the
		 * result in rm
		 *
		 * @param rm The float array to store the result
		 * @param rmOffset The offset into rm where the matrix should start
		 * @param m The float array that holds matrix to rotate
		 * @param mOffset The offset into <code>m</code> where the matrix starts
		 * @param w The w-component of the quaternion
		 * @param x The x-component of the quaternion
		 * @param y The y-component of the quaternion
		 * @param z The z-component of the quaternion
		 */
		public static void rotateQuaternionM(float[] rm, int rmOffset, float[] m, int mOffset, float w, float x, float y, float z) {
			synchronized (sTemp1) {
				setRotateQuaternionM(sTemp1, 0, w, x, y, z);
				multiplyMM(rm, rmOffset, m, mOffset, sTemp1, 0);
			}
		}

		/**
		 * Converts a quaternion (w, x, y, z) to a rotation matrix.
		 *
		 * @param m The float array that holds matrix to rotate
		 * @param mOffset The offset into <code>m</code> where the matrix starts
		 * @param w The w-component of the quaternion
		 * @param x The x-component of the quaternion
		 * @param y The y-component of the quaternion
		 * @param z The z-component of the quaternion
		 */
		public static void setRotateQuaternionM(float[] m, int mOffset, float w, float x, float y, float z) {
			if (m.length < mOffset + 16) {
				throw new IllegalArgumentException("m.length < mOffset + 16");
			}
			final float xx = x*x, yy = y*y, zz = z*z;
			final float xy = x*y, yz = y*z, xz = x*z;
			final float xw = x*w, yw = y*w, zw = z*w;
			m[mOffset +  0] = 1 - 2*yy - 2*zz;
			m[mOffset +  1] = 2*xy + 2*zw;
			m[mOffset +  2] = 2*xz - 2*yw;
			m[mOffset +  3] = 0;

			m[mOffset +  4] = 2*xy - 2*zw;
			m[mOffset +  5] = 1 - 2*xx - 2*zz;
			m[mOffset +  6] = 2*yz + 2*xw;
			m[mOffset +  7] = 0;

			m[mOffset +  8] = 2*xz + 2*yw;
			m[mOffset +  9] = 2*yz - 2*xw;
			m[mOffset + 10] = 1 - 2*xx - 2*yy;
			m[mOffset + 11] = 0;

			m[mOffset + 12] = 0;
			m[mOffset + 13] = 0;
			m[mOffset + 14] = 0;
			m[mOffset + 15] = 1;
		}
	}

}
