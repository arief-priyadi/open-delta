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
package au.org.ala.delta.ui.rtf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import junit.framework.TestCase;

import org.junit.Test;

import au.org.ala.delta.ui.rtf.SimpleRtfEditorKit;


/**
 * Tests the SimpleRtfEditorKit
 */
public class SimpleRtfEditorKitTest extends TestCase {
	
	private static final String WRITER_HEADER_TEXT = "{\\rtf\\ansi\\pard\\plain ";
	
	@Test public void testReadSimpleDocument() throws Exception {
		SimpleRtfEditorKit editorKit = new SimpleRtfEditorKit(null);
		
		DefaultStyledDocument doc = new DefaultStyledDocument();
		InputStream in = getClass().getResourceAsStream("/rtf/test1.rtf");
		
		editorKit.read(in, doc, 0);
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		editorKit.write(bout, doc, 0, doc.getLength());
		
		bout.flush();
		String documentAsString = new String(bout.toByteArray());
		
		assertEquals(WRITER_HEADER_TEXT+"This is plain text.\\par \\par }", documentAsString);
		
	}
	
	@Test public void testReadSimpleFormattedDocument() throws Exception {
		SimpleRtfEditorKit editorKit = new SimpleRtfEditorKit(null);
		
		DefaultStyledDocument doc = new DefaultStyledDocument();
		InputStream in = getClass().getResourceAsStream("/rtf/test2.rtf");
		
		editorKit.read(in, doc, 0);
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		editorKit.write(bout, doc, 0, doc.getLength());
		
		bout.flush();
		String documentAsString = new String(bout.toByteArray());
		
		System.out.println(documentAsString);
		
		
	}
	
	public void testMoreComplexDocument() throws Exception {
		String rtf = "{\\rtf\\ansi\\deff0{\\fonttbl{\\f0\\froman Tms Rmn;}}\\pard\\plain \\fs20 \\super This is plain text. \\super0\\par{\\b\\i This is bold italic}}";
		
		SimpleRtfEditorKit editorKit = new SimpleRtfEditorKit(null);
		DefaultStyledDocument doc = new DefaultStyledDocument();
		InputStream in = new ByteArrayInputStream(rtf.getBytes());
		editorKit.read(in, doc, 0);
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		editorKit.write(bout, doc, 0, doc.getLength());
		
		bout.flush();
		String documentAsString = new String(bout.toByteArray());
		
		// The order in which the attributes are processed and hence emitted is not deterministic
		// so the assertions are more fiddly...
		int offset = 0;
		assertEquals(WRITER_HEADER_TEXT, documentAsString.substring(offset, WRITER_HEADER_TEXT.length()));
		offset += WRITER_HEADER_TEXT.length();
		assertEquals("\\super", documentAsString.substring(offset, offset+6));
		assertEquals(" This is plain text. ", documentAsString.substring(offset+6, offset+27));
		assertTrue(documentAsString.substring(offset+27, offset+48).contains("\\b"));
		assertTrue(documentAsString.substring(offset+27, offset+48).contains("\\i"));
		assertTrue(documentAsString.substring(offset+27, offset+48).contains("\\nosupersub"));
		assertEquals(" This is bold italic", documentAsString.substring(offset+48, offset+68));
		assertTrue(documentAsString.substring(offset+68, documentAsString.length()).contains("\\i0"));
		assertTrue(documentAsString.substring(offset+68, documentAsString.length()).contains("\\b0"));
	}
	
	public void testUnicode() throws Exception {
		String rtf = "{\\rtf\\ansi\\deff0{\\fonttbl{\\f0\\froman Tms Rmn;}}\\pard\\plain This is \\u2222? text.}";
		String documentAsString = readAndWrite(rtf);

		assertEquals(WRITER_HEADER_TEXT+"This is \\u2222? text.\\par }", documentAsString);
	}
	
	public void testUnderline() throws Exception {
		String rtf = "{\\rtf\\ansi\\deff0{\\fonttbl{\\f0\\froman Tms Rmn;}}\\pard\\plain This is \\ul underlined\\ul0 text.}";
		String documentAsString = readAndWrite(rtf);

		assertEquals(WRITER_HEADER_TEXT+"This is \\ul underlined\\ul0 text.\\par }", documentAsString);
	}

	/**
	 * Writes the supplied String using the editor kit, then reads it back and returns
	 * the read string.
	 * @param rtf the string to write.
	 * @return the string after it's been read back.
	 */
	private String readAndWrite(String rtf) throws IOException, BadLocationException {
		StringReader reader = new StringReader(rtf);
		SimpleRtfEditorKit editorKit = new SimpleRtfEditorKit(null);
		DefaultStyledDocument doc = new DefaultStyledDocument();
		editorKit.read(reader, doc, 0);
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		editorKit.write(bout, doc, 0, doc.getLength());
		
		bout.flush();
		String documentAsString = new String(bout.toByteArray());
		return documentAsString;
	}
	
	
}
