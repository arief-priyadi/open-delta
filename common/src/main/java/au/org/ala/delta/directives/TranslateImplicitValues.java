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

import au.org.ala.delta.DeltaContext;
import au.org.ala.delta.directives.args.DirectiveArguments;
import au.org.ala.delta.translation.DataSetTranslatorFactory;
import au.org.ala.delta.translation.naturallanguage.ImplicitValuesTranslator;

/**
 * Handles the TRANSLATE IMPLICT VALUES directive.
 * @see http://delta-intkey.com/www/uguide.htm#_*INSERT_IMPLICIT_VALUES
 */
public class TranslateImplicitValues extends AbstractNoArgDirective {

	DataSetTranslatorFactory factory;
	
	public TranslateImplicitValues() {
		super("translate", "implicit", "values");
		factory = new DataSetTranslatorFactory();
	}
	
	@Override
	public void process(DeltaContext context, DirectiveArguments data) throws Exception {
		ImplicitValuesTranslator translator = factory.createImplicitValuesTranslator(context);
		translator.translateImplicitValues();
	}
	
	@Override
	public int getOrder() {
		return 4;
	}
}
