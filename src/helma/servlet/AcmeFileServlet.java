// FileServlet - servlet similar to a standard httpd
//
// Copyright (C)1996,1998 by Jef Poskanzer <jef@acme.com>. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
// OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
// LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
// OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.
//
// Visit the ACME Labs Java page for up-to-date versions of this and other
// fine Java utilities: http://www.acme.com/java/

package helma.servlet;

import java.io.*;
import java.util.*;
import java.text.*;
import Acme.Serve.*;
import javax.servlet.*;
import javax.servlet.http.*;

/// Servlet similar to a standard httpd.
// <P>
// Implements the "GET" and "HEAD" methods for files and directories.
// Handles index.html.
// Redirects directory URLs that lack a trailing /.
// Handles If-Modified-Since and Range.
// <P>
// <A HREF="/resources/classes/Acme/Serve/FileServlet.java">Fetch the software.</A><BR>
// <A HREF="/resources/classes/Acme.tar.gz">Fetch the entire Acme package.</A>
// <P>
// @see Acme.Serve.Serve

public class AcmeFileServlet extends FileServlet
    {

    private File root;


    /// Constructor.
    public AcmeFileServlet(File root)
	{
	super ();
	this.root = root;
	}

    public void init (ServletConfig config) throws ServletException {
	super.init (config);
	// do nothing
    }


     /// Services a single request from the client.
    // @param req the servlet request
    // @param req the servlet response
    // @exception ServletException when an exception has occurred
    public void service( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException
	{
	boolean headOnly;
	if ( req.getMethod().equalsIgnoreCase( "get" ) )
	    headOnly = false;
	else if ( ! req.getMethod().equalsIgnoreCase( "head" ) )
	    headOnly = true;
	else
	    {
	    res.sendError( HttpServletResponse.SC_NOT_IMPLEMENTED );
	    return;
	    }

	String path = req.getServletPath();
	if ( path == null || path.charAt( 0 ) != '/' )
	    {
	    res.sendError( HttpServletResponse.SC_BAD_REQUEST );
	    return;
	    }
	if ( path.indexOf( "/../" ) != -1 || path.endsWith( "/.." ) )
	    {
	    res.sendError( HttpServletResponse.SC_FORBIDDEN );
	    return;
	    }

	// Make a version without the leading /.
	String pathname = path.substring( 1 );
	if ( pathname.length() == 0 )
	    pathname = "./";

	dispatchPathname( req, res, headOnly, path, pathname );
	}


    protected void dispatchPathname( HttpServletRequest req, HttpServletResponse res, boolean headOnly, String path, String pathname ) throws IOException
	{
	String filename = pathname.replace( '/', File.separatorChar );
	if ( filename.charAt( filename.length() - 1 ) == File.separatorChar )
	    filename = filename.substring( 0, filename.length() - 1 );
	if (filename.startsWith ("static"))
	    filename = filename.substring ( Math.min (7, filename.length())  );

	File file = new File( root, filename );

	if ( file.exists() )
	    {
	    if ( ! file.isDirectory() )
		serveFile( req, res, headOnly, path, filename, file );
	    else
		{
		if ( pathname.charAt( pathname.length() - 1 ) != '/' )
		    redirectDirectory( req, res, path, file );
		else
		    {
		    String indexFilename =
			filename + File.separatorChar + "index.html";
		    File indexFile = new File( indexFilename );
		    if ( indexFile.exists() )
			serveFile(
			    req, res, headOnly, path, indexFilename,
			    indexFile );
		    else
			serveDirectory(
			    req, res, headOnly, path, filename, file );
		    }
		}
	    }
	else
	    {
	    if ( pathname.endsWith( "/index.html" ) )
		dispatchPathname(
		    req, res, headOnly, path,
		    pathname.substring( 0, pathname.length() - 10 ) );
	    else if ( pathname.equals( "index.html" ) )
		dispatchPathname( req, res, headOnly, path, "./" );
	    else
		res.sendError( HttpServletResponse.SC_NOT_FOUND );
	    }
	}




    private void serveDirectory( HttpServletRequest req, HttpServletResponse res, boolean headOnly, String path, String filename, File file ) throws IOException
	{
	log( "indexing " + path );
	if ( ! file.canRead() )
	    {
	    res.sendError( HttpServletResponse.SC_FORBIDDEN );
	    return;
	    }
	res.setStatus( HttpServletResponse.SC_OK );
	res.setContentType( "text/html" );
	OutputStream out = res.getOutputStream();
	if ( ! headOnly )
	    {
	    PrintStream p = new PrintStream( new BufferedOutputStream( out ) );
	    p.println( "<HTML><HEAD>" );
	    p.println( "<TITLE>Index of " + path + "</TITLE>" );
	    p.println( "</HEAD><BODY BGCOLOR=\"#ffffff\">" );
	    p.println( "<H2>Index of " + path + "</H2>" );
	    p.println( "<PRE>" );
	    p.println( "mode     bytes  last-changed  name" );
	    p.println( "<HR>" );
	    String[] names = file.list();
	    Acme.Utils.sortStrings( names );
	    for ( int i = 0; i < names.length; ++i )
		{
		String aFilename = filename + File.separatorChar + names[i];
		File aFile = new File( aFilename );
		String aFileType;
		if ( aFile.isDirectory() )
		    aFileType = "d";
		else if ( aFile.isFile() )
		    aFileType = "-";
		else
		    aFileType = "?";
		String aFileRead = ( aFile.canRead() ? "r" : "-" );
		String aFileWrite = ( aFile.canWrite() ? "w" : "-" );
		String aFileExe = "-";
		String aFileSize = Acme.Fmt.fmt( aFile.length(), 8 );
		String aFileDate =
		    Acme.Utils.lsDateStr( new Date( aFile.lastModified() ) );
		String aFileDirsuf = ( aFile.isDirectory() ? "/" : "" );
		String aFileSuf = ( aFile.isDirectory() ? "/" : "" );
		p.println(
		    aFileType + aFileRead + aFileWrite + aFileExe +
		    "  " + aFileSize + "  " + aFileDate + "  " +
		    "<A HREF=\"" + names[i] + aFileDirsuf + "\">" +
		    names[i] + aFileSuf + "</A>" );
		}
	    p.println( "</PRE>" );
	    p.println( "<HR>" );
	    ServeUtils.writeAddress( p );
	    p.println( "</BODY></HTML>" );
	    p.flush();
	    }
	out.close();
	}

    
    }
