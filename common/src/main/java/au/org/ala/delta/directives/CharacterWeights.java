/*******************************************************************************
 * Copyright (C) 2011 Atlas of Living Australia
 * All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ******************************************************************************/
package au.org.ala.delta.directives;

import java.math.BigDecimal;


/**
 * Processes the CHARACTER RELIABILITES directive.
 */
public class CharacterWeights extends CharacterWeightDirective {

	public static final String[] CONTROL_WORDS =  {"character", "weights"};
	
	/** The default weight for any characters not included in this directive */
	private static final BigDecimal DEFAULT_WEIGHT = new BigDecimal("1");
	
	/** The minimum allowed weight for a character */
	private static final double MIN_WEIGHT = 0.03125d;
	
	/** The maximum allowed weight for a character */
	private static final double MAX_WEIGHT = 32d;
	
	public CharacterWeights() {
		super(MIN_WEIGHT, MAX_WEIGHT, DEFAULT_WEIGHT, CONTROL_WORDS);
	}
	
	@Override
	public int getOrder() {
		return 4;
	}
}
