package com.vodimo.core.model;

import org.apache.commons.lang3.ArrayUtils;

public class TransitionIndex {
	
	/* 
	 * The starting bits
	 */
	private int bits = 0;
		
	/*
	 * Bit mask starting at far most left bit
	 */
	private int mask; 
	
	/*
	 * Transition memory size
	 */
	private int memorySize;
	
	public TransitionIndex(int _memorySize) {
		this.memorySize = _memorySize;
		this.mask = (int) (Math.pow(2, this.memorySize - 1));
	}
	
	public TransitionIndex(int _memorySize, int bits) {
		this.memorySize = _memorySize;
		this.mask = (int) (Math.pow(2, this.memorySize - 1));
		this.bits = bits;
	}
			
		
	public void flip(int flipPosition) {
		int flipMask = mask << this.memorySize;
		flipMask >>= flipPosition;
		bits ^= flipMask;		
	}
	
	public void shift() {		
		int sMask = (int) Math.pow(2, this.memorySize) - 1;
		//System.out.println("shift() mask = " + sMask);
		//sMask <<= this.memorySize;
		//System.out.println("shift() mask <<= this.memorySize = " + Integer.toBinaryString(sMask));
		bits = bits & sMask;
		bits <<= this.memorySize;
		//System.out.println("bits = " + Integer.toBinaryString((bits)));
	}
	
	public int getFirstTransition() {
		return bits >>> this.memorySize;	
	}
	
	public int getSecondTransition() {
		int sMask = (int) Math.pow(2, this.memorySize) - 1;
		return bits & sMask;		
	}
	
	public int getLastBit() {
		int second = getSecondTransition();
		int sMask = 1;
		return sMask & second;
	}
	
	//******************************************************************
	// Static Methods
	//******************************************************************	
	
	/*
	 * Converts a set of bits into an Integer[] of 1s and 0s
	 */
	// TODO: Need to confirm that this works properly 
	public static int[] convertToIntegerArray(int bits) {
		int size = length(bits);	
		int[] arr = new int[size];
		for(int i=(size - 1);i>=0;i--) {
			if(((bits >>> i) & 1) == 1) {
				arr[i] = 1;
			} else {
				arr[i] = 0;
			}
		}
		//Reverse the array
		ArrayUtils.reverse(arr);
		return arr;
	}
	
	public static int length(int bits){
	    if (bits == 0) {
	        return 0;
	    }
	    
	    int l = 1;
	    if (bits >>> 16 > 0) { bits >>= 16; l += 16; }
	    if (bits >>> 8 > 0) { bits >>= 8; l += 8; }
	    if (bits >>> 4 > 0) { bits >>= 4; l += 4; }
	    if (bits >>> 2 > 0) { bits >>= 2; l += 2; }
	    if (bits >>> 1 > 0) { bits >>= 1; l += 1; }
	    
	    //System.out.println("length = " + l);	    
	    return l;
	}

	@Override
	public int hashCode() {
		//System.out.println("bits = " + Integer.toBinaryString(bits));
		return bits;
	}
	
}
