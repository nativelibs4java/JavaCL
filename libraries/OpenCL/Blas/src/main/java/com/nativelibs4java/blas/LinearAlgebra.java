/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.nativelibs4java.blas;

/**
 *
 * @author Olivier
 */
public interface LinearAlgebra<M extends Matrix> {
    M newMatrix(int rows, int columns);

	M newMatrix(Matrix m);
}