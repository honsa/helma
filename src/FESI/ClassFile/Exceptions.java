/*
 *
 * @(#) Exceptions.java 1.3@(#)
 *
 * Copyright (c) 1997 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 * 
 */

/**
 * <p>
 * FESI.ClassFile.Exceptions
 * </p> 
 *
 * @version 1.0
 * @author Laurence P. G. Cable
 */


package FESI.ClassFile;

import java.io.DataOutputStream;
import java.io.IOException;

import FESI.ClassFile.Attribute;
import FESI.ClassFile.ClassFile;
import FESI.ClassFile.ClassConstant;

/**
 * <p>
 * The Exceptions class extends the Attribute class to enumerate the
 * exception types generated by method implementations in a class file
 * </p>
 */

class Exceptions extends Attribute {

    private ClassConstant[] exceptions;

    /**
     * <p> construct an Exceptions attribute that enumerates the exceptions </p>
     *
     * @param exs[]	an array of exception class constants
     * @param cf	the containing class file
     */

    Exceptions(ClassConstant[] exs, ClassFile cf) {
    	super(Attribute.EXCEPTIONS, cf);

    	// we should validate that the ClassConstants are all
    	// subclasses of Exception here ...

    	exceptions = exs;
    }

    /**
     * <p> construct an Exceptions attribute that enumerates the exceptions </p>
     *
     * @param exs[]	an array of exception class types
     * @param cf	the containing class file
     *
     */
    Exceptions(Class[] exs, ClassFile cf) {
    	super(Attribute.EXCEPTIONS, cf);

    	// we should validate that the ClassConstants are all
    	// subclasses of Exception here ...

	ClassConstant[] cc = new ClassConstant[exs.length];

	for (int i = 0; i < exs.length; i++)
		cc[i] = cf.addClassConstant(exs[i].getName());

	exceptions = cc;
    }

    /**
     * <p> write the Exceptions attribute to the stream </p>
     *
     * @param dos the output stream
     *
     * @throws IOException
     */

    void write(DataOutputStream dos) throws IOException {
    	dos.writeShort(getNameConstantPoolIndex());
    	dos.writeInt(getLength());
	
	if (exceptions != null && exceptions.length > 0) {
    	    dos.writeShort(exceptions.length);

    	    for (int i = 0; i < exceptions.length; i++) {
		dos.writeShort(exceptions[i].getConstantPoolIndex());
    	    }
	} else dos.writeShort(0);
    }

    /**
     * @return the Object's equality
     */

    public boolean equals(Object o) {
    	if (o instanceof Exceptions) {
    	    Exceptions other = (Exceptions)o;

    	    if (exceptions.length == other.exceptions.length) {
    	        for (int i = 0; i < exceptions.length; i++) {
    	    	    if (!exceptions[i].equals(other.exceptions[i]))
    			return false;
    		    }

    		return true;
	    }
    	 }

    	return false;
    }

    /**
     * @return the length of the Attribute in bytes
     */

    int getLength() { return exceptions.length * 2 + 2; }

    /**
     * <p> adds exception class to the attribute. </p>
     *
     * @param a class constant to add to the attribute
     *
     */

    void addException(ClassConstant ex) {

	// should verify that ClassConstant is exception subclass and not
	// already in the attribute

	if (exceptions == null) {
	    exceptions = new ClassConstant[1];

	    exceptions[0] = ex;
	} else {
	    ClassConstant[] temp = new ClassConstant[exceptions.length + 1];
	    int             i;

	    for (i = 0; i < exceptions.length; i++) {
		temp[i] = exceptions[i];
	    }

	    temp[i] = ex;

	    exceptions = temp;
	}
    }
}
