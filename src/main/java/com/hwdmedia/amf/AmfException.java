/**
 * Copyright © 2013 Gennadiy Kozlenko
 * 
 * This file is part of hwdmedia-amf - AMF reader/writer library.
 */
package com.hwdmedia.amf;

/**
 * AMF Exception
 * 
 * @author Gennadiy Kozlenko
 */
public class AmfException extends Exception {
    
    public AmfException() {
        super();
    }

    public AmfException(String err) {
        super(err);
    }
    
    public AmfException(Throwable cause) {
        super(cause);
    }
    
    public AmfException(String err, Throwable cause) {
        super(err, cause);
    }
    
}
