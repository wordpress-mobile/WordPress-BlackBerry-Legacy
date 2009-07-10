package com.wordpress.utils;

/*-
 * Copyright (c) 2009, Derek Konigsberg
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */


import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

/**
 * Common front-end for building strings based on input data
 * in a wide variety of character forms.  This implementation
 * is necessary because the built-in String class on the platform
 * has insufficient support for international character forms.
 *
 * <p>
 * In addition to character encodings supported by the underlying platform,
 * support is included for the following additional encodings:
 * </p>
 * <ul>
 * <li><b>&quot;Windows-1250&quot;</b> - Central European and Eastern European languages that use Latin script</li>
 * <li><b>&quot;Windows-1251&quot;</b> - Languages that use the Cyrillic alphabet</li>
 * <li><b>&quot;Windows-1252&quot;</b> - English and some other Western languages</li>
 * <li><b>&quot;Windows-1253&quot;</b> - Modern Greek</li>
 * <li><b>&quot;Windows-1254&quot;</b> - Turkish</li>
 * <li><b>&quot;Windows-1255&quot;</b> - Hebrew</li>
 * <li><b>&quot;Windows-1256&quot;</b> - Arabic</li>
 * <li><b>&quot;Windows-1257&quot;</b> - Estonian, Latvian and Lithuanian</li>
 * <li><b>&quot;Windows-1258&quot;</b> - Vietnamese</li>
 * </ul>
 */
public class StringFactory {
    private static StringFactory instance;
    private Hashtable charsetMappingTables;

    /**
     * Instantiates a new string factory instance.
     */
    private StringFactory() {
        charsetMappingTables = new Hashtable();
        charsetMappingTables.put("CP1250", mappingTableCP1250);
        charsetMappingTables.put("WINDOWS-1250", mappingTableCP1250);
        charsetMappingTables.put("CP1251", mappingTableCP1251);
        charsetMappingTables.put("WINDOWS-1251", mappingTableCP1251);
        charsetMappingTables.put("CP1252", mappingTableCP1252);
        charsetMappingTables.put("WINDOWS-1252", mappingTableCP1252);
        charsetMappingTables.put("CP1253", mappingTableCP1253);
        charsetMappingTables.put("WINDOWS-1253", mappingTableCP1253);
        charsetMappingTables.put("CP1254", mappingTableCP1254);
        charsetMappingTables.put("WINDOWS-1254", mappingTableCP1254);
        charsetMappingTables.put("CP1255", mappingTableCP1255);
        charsetMappingTables.put("WINDOWS-1255", mappingTableCP1255);
        charsetMappingTables.put("CP1256", mappingTableCP1256);
        charsetMappingTables.put("WINDOWS-1256", mappingTableCP1256);
        charsetMappingTables.put("CP1257", mappingTableCP1257);
        charsetMappingTables.put("WINDOWS-1257", mappingTableCP1257);
        charsetMappingTables.put("CP1258", mappingTableCP1258);
        charsetMappingTables.put("WINDOWS-1258", mappingTableCP1258);
    }

    /**
     * Gets the single instance of StringFactory.
     * 
     * @return single instance of StringFactory
     */
    private static synchronized StringFactory getInstance() {
        if(instance == null) {
            instance = new StringFactory();
        }
        return instance;
    }

    /**
     * Construct a new <tt>String</tt> by converting the specified array of
     * bytes using the specified character encoding.  This method will first
     * attempt to use the constructor of the <tt>String</tt> class that takes
     * a character encoding parameter.  If <tt>String</tt> does not directly
     * recognize the encoding, this method will then attempt to use a variety
     * of Unicode mapping tables.
     *
     * @param bytes The bytes to be converted into characters
     * @param charset The name of a supported character encoding
     * @return Converted string representation
     * @throws UnsupportedEncodingException If the named character encoding is not supported
     * 
     * @see String#String(byte[], String)
     */
    public static String create(byte[] bytes, String charset) throws UnsupportedEncodingException {
        String result;
        try {
            result = new String(bytes, charset);
        } catch (UnsupportedEncodingException exp) {
            result = getInstance().createFromMappingTable(bytes, charset);
        }
        return result;
    }
   
    /**
     * Construct a new <tt>String</tt> by converting the specified array of
     * bytes using the specified character encoding.
     *
     * @param bytes The bytes to be converted into characters
     * @param charset The name of a supported character encoding
     * @return Converted string representation
     * @throws UnsupportedEncodingException If the named character encoding is not supported
     */
    private String createFromMappingTable(byte[] bytes, String charset) throws UnsupportedEncodingException {
        char[] mappingTable = (char[])charsetMappingTables.get(charset.toUpperCase());
        if(mappingTable == null) {
            throw new UnsupportedEncodingException();
        }
        
        StringBuffer buf = new StringBuffer();
        for(int i=0; i<bytes.length; i++) {
            buf.append(mappingTable[(int) bytes[i] & 0xFF]);
        }
        return buf.toString();
    }
    
    /**
     * cp1250 to Unicode table
     * <pre>
     * Unicode version: 2.0
     * Table version: 2.01
     * Date: 04/15/98
     * URL:  http://www.unicode.org/Public/MAPPINGS/VENDORS/MICSFT/WINDOWS/CP1250.TXT
     * </pre>
     */
    private static char[] mappingTableCP1250 = {
        '\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005',
        '\u0006', '\u0007', '\u0008', '\u0009', '\n', '\u000B',
        '\u000C', '\r', '\u000E', '\u000F', '\u0010', '\u0011',
        '\u0012', '\u0013', '\u0014', '\u0015', '\u0016', '\u0017',
        '\u0018', '\u0019', '\u001A', '\u001B', '\u001C', '\u001D',
        '\u001E', '\u001F', '\u0020', '\u0021', '\u0022', '\u0023',
        '\u0024', '\u0025', '\u0026', '\'', '\u0028', '\u0029',
        '\u002A', '\u002B', '\u002C', '\u002D', '\u002E', '\u002F',
        '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035',
        '\u0036', '\u0037', '\u0038', '\u0039', '\u003A', '\u003B',
        '\u003C', '\u003D', '\u003E', '\u003F', '\u0040', '\u0041',
        '\u0042', '\u0043', '\u0044', '\u0045', '\u0046', '\u0047',
        '\u0048', '\u0049', '\u004A', '\u004B', '\u004C', '\u004D',
        '\u004E', '\u004F', '\u0050', '\u0051', '\u0052', '\u0053',
        '\u0054', '\u0055', '\u0056', '\u0057', '\u0058', '\u0059',
        '\u005A', '\u005B', '\\', '\u005D', '\u005E', '\u005F',
        '\u0060', '\u0061', '\u0062', '\u0063', '\u0064', '\u0065',
        '\u0066', '\u0067', '\u0068', '\u0069', '\u006A', '\u006B',
        '\u006C', '\u006D', '\u006E', '\u006F', '\u0070', '\u0071',
        '\u0072', '\u0073', '\u0074', '\u0075', '\u0076', '\u0077',
        '\u0078', '\u0079', '\u007A', '\u007B', '\u007C', '\u007D',
        '\u007E', '\u007F', '\u20AC', '\uFFFD', '\u201A', '\uFFFD',
        '\u201E', '\u2026', '\u2020', '\u2021', '\uFFFD', '\u2030',
        '\u0160', '\u2039', '\u015A', '\u0164', '\u017D', '\u0179',
        '\uFFFD', '\u2018', '\u2019', '\u201C', '\u201D', '\u2022',
        '\u2013', '\u2014', '\uFFFD', '\u2122', '\u0161', '\u203A',
        '\u015B', '\u0165', '\u017E', '\u017A', '\u00A0', '\u02C7',
        '\u02D8', '\u0141', '\u00A4', '\u0104', '\u00A6', '\u00A7',
        '\u00A8', '\u00A9', '\u015E', '\u00AB', '\u00AC', '\u00AD',
        '\u00AE', '\u017B', '\u00B0', '\u00B1', '\u02DB', '\u0142',
        '\u00B4', '\u00B5', '\u00B6', '\u00B7', '\u00B8', '\u0105',
        '\u015F', '\u00BB', '\u013D', '\u02DD', '\u013E', '\u017C',
        '\u0154', '\u00C1', '\u00C2', '\u0102', '\u00C4', '\u0139',
        '\u0106', '\u00C7', '\u010C', '\u00C9', '\u0118', '\u00CB',
        '\u011A', '\u00CD', '\u00CE', '\u010E', '\u0110', '\u0143',
        '\u0147', '\u00D3', '\u00D4', '\u0150', '\u00D6', '\u00D7',
        '\u0158', '\u016E', '\u00DA', '\u0170', '\u00DC', '\u00DD',
        '\u0162', '\u00DF', '\u0155', '\u00E1', '\u00E2', '\u0103',
        '\u00E4', '\u013A', '\u0107', '\u00E7', '\u010D', '\u00E9',
        '\u0119', '\u00EB', '\u011B', '\u00ED', '\u00EE', '\u010F',
        '\u0111', '\u0144', '\u0148', '\u00F3', '\u00F4', '\u0151',
        '\u00F6', '\u00F7', '\u0159', '\u016F', '\u00FA', '\u0171',
        '\u00FC', '\u00FD', '\u0163', '\u02D9'
    };

    /**
     * cp1251 to Unicode table
     * <pre>
     * Unicode version: 2.0
     * Table version: 2.01
     * Date: 04/15/98
     * URL:  http://www.unicode.org/Public/MAPPINGS/VENDORS/MICSFT/WINDOWS/CP1251.TXT
     * </pre>
     */
    private static char[] mappingTableCP1251 = {
        '\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005',
        '\u0006', '\u0007', '\u0008', '\u0009', '\n', '\u000B',
        '\u000C', '\r', '\u000E', '\u000F', '\u0010', '\u0011',
        '\u0012', '\u0013', '\u0014', '\u0015', '\u0016', '\u0017',
        '\u0018', '\u0019', '\u001A', '\u001B', '\u001C', '\u001D',
        '\u001E', '\u001F', '\u0020', '\u0021', '\u0022', '\u0023',
        '\u0024', '\u0025', '\u0026', '\'', '\u0028', '\u0029',
        '\u002A', '\u002B', '\u002C', '\u002D', '\u002E', '\u002F',
        '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035',
        '\u0036', '\u0037', '\u0038', '\u0039', '\u003A', '\u003B',
        '\u003C', '\u003D', '\u003E', '\u003F', '\u0040', '\u0041',
        '\u0042', '\u0043', '\u0044', '\u0045', '\u0046', '\u0047',
        '\u0048', '\u0049', '\u004A', '\u004B', '\u004C', '\u004D',
        '\u004E', '\u004F', '\u0050', '\u0051', '\u0052', '\u0053',
        '\u0054', '\u0055', '\u0056', '\u0057', '\u0058', '\u0059',
        '\u005A', '\u005B', '\\', '\u005D', '\u005E', '\u005F',
        '\u0060', '\u0061', '\u0062', '\u0063', '\u0064', '\u0065',
        '\u0066', '\u0067', '\u0068', '\u0069', '\u006A', '\u006B',
        '\u006C', '\u006D', '\u006E', '\u006F', '\u0070', '\u0071',
        '\u0072', '\u0073', '\u0074', '\u0075', '\u0076', '\u0077',
        '\u0078', '\u0079', '\u007A', '\u007B', '\u007C', '\u007D',
        '\u007E', '\u007F', '\u0402', '\u0403', '\u201A', '\u0453',
        '\u201E', '\u2026', '\u2020', '\u2021', '\u20AC', '\u2030',
        '\u0409', '\u2039', '\u040A', '\u040C', '\u040B', '\u040F',
        '\u0452', '\u2018', '\u2019', '\u201C', '\u201D', '\u2022',
        '\u2013', '\u2014', '\uFFFD', '\u2122', '\u0459', '\u203A',
        '\u045A', '\u045C', '\u045B', '\u045F', '\u00A0', '\u040E',
        '\u045E', '\u0408', '\u00A4', '\u0490', '\u00A6', '\u00A7',
        '\u0401', '\u00A9', '\u0404', '\u00AB', '\u00AC', '\u00AD',
        '\u00AE', '\u0407', '\u00B0', '\u00B1', '\u0406', '\u0456',
        '\u0491', '\u00B5', '\u00B6', '\u00B7', '\u0451', '\u2116',
        '\u0454', '\u00BB', '\u0458', '\u0405', '\u0455', '\u0457',
        '\u0410', '\u0411', '\u0412', '\u0413', '\u0414', '\u0415',
        '\u0416', '\u0417', '\u0418', '\u0419', '\u041A', '\u041B',
        '\u041C', '\u041D', '\u041E', '\u041F', '\u0420', '\u0421',
        '\u0422', '\u0423', '\u0424', '\u0425', '\u0426', '\u0427',
        '\u0428', '\u0429', '\u042A', '\u042B', '\u042C', '\u042D',
        '\u042E', '\u042F', '\u0430', '\u0431', '\u0432', '\u0433',
        '\u0434', '\u0435', '\u0436', '\u0437', '\u0438', '\u0439',
        '\u043A', '\u043B', '\u043C', '\u043D', '\u043E', '\u043F',
        '\u0440', '\u0441', '\u0442', '\u0443', '\u0444', '\u0445',
        '\u0446', '\u0447', '\u0448', '\u0449', '\u044A', '\u044B',
        '\u044C', '\u044D', '\u044E', '\u044F'
    };

    /**
     * cp1252 to Unicode table
     * <pre>
     * Unicode version: 2.0
     * Table version: 2.01
     * Date: 04/15/98
     * URL:  http://www.unicode.org/Public/MAPPINGS/VENDORS/MICSFT/WINDOWS/CP1252.TXT
     * </pre>
     */
    private static char[] mappingTableCP1252 = {
        '\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005',
        '\u0006', '\u0007', '\u0008', '\u0009', '\n', '\u000B',
        '\u000C', '\r', '\u000E', '\u000F', '\u0010', '\u0011',
        '\u0012', '\u0013', '\u0014', '\u0015', '\u0016', '\u0017',
        '\u0018', '\u0019', '\u001A', '\u001B', '\u001C', '\u001D',
        '\u001E', '\u001F', '\u0020', '\u0021', '\u0022', '\u0023',
        '\u0024', '\u0025', '\u0026', '\'', '\u0028', '\u0029',
        '\u002A', '\u002B', '\u002C', '\u002D', '\u002E', '\u002F',
        '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035',
        '\u0036', '\u0037', '\u0038', '\u0039', '\u003A', '\u003B',
        '\u003C', '\u003D', '\u003E', '\u003F', '\u0040', '\u0041',
        '\u0042', '\u0043', '\u0044', '\u0045', '\u0046', '\u0047',
        '\u0048', '\u0049', '\u004A', '\u004B', '\u004C', '\u004D',
        '\u004E', '\u004F', '\u0050', '\u0051', '\u0052', '\u0053',
        '\u0054', '\u0055', '\u0056', '\u0057', '\u0058', '\u0059',
        '\u005A', '\u005B', '\\', '\u005D', '\u005E', '\u005F',
        '\u0060', '\u0061', '\u0062', '\u0063', '\u0064', '\u0065',
        '\u0066', '\u0067', '\u0068', '\u0069', '\u006A', '\u006B',
        '\u006C', '\u006D', '\u006E', '\u006F', '\u0070', '\u0071',
        '\u0072', '\u0073', '\u0074', '\u0075', '\u0076', '\u0077',
        '\u0078', '\u0079', '\u007A', '\u007B', '\u007C', '\u007D',
        '\u007E', '\u007F', '\u20AC', '\uFFFD', '\u201A', '\u0192',
        '\u201E', '\u2026', '\u2020', '\u2021', '\u02C6', '\u2030',
        '\u0160', '\u2039', '\u0152', '\uFFFD', '\u017D', '\uFFFD',
        '\uFFFD', '\u2018', '\u2019', '\u201C', '\u201D', '\u2022',
        '\u2013', '\u2014', '\u02DC', '\u2122', '\u0161', '\u203A',
        '\u0153', '\uFFFD', '\u017E', '\u0178', '\u00A0', '\u00A1',
        '\u00A2', '\u00A3', '\u00A4', '\u00A5', '\u00A6', '\u00A7',
        '\u00A8', '\u00A9', '\u00AA', '\u00AB', '\u00AC', '\u00AD',
        '\u00AE', '\u00AF', '\u00B0', '\u00B1', '\u00B2', '\u00B3',
        '\u00B4', '\u00B5', '\u00B6', '\u00B7', '\u00B8', '\u00B9',
        '\u00BA', '\u00BB', '\u00BC', '\u00BD', '\u00BE', '\u00BF',
        '\u00C0', '\u00C1', '\u00C2', '\u00C3', '\u00C4', '\u00C5',
        '\u00C6', '\u00C7', '\u00C8', '\u00C9', '\u00CA', '\u00CB',
        '\u00CC', '\u00CD', '\u00CE', '\u00CF', '\u00D0', '\u00D1',
        '\u00D2', '\u00D3', '\u00D4', '\u00D5', '\u00D6', '\u00D7',
        '\u00D8', '\u00D9', '\u00DA', '\u00DB', '\u00DC', '\u00DD',
        '\u00DE', '\u00DF', '\u00E0', '\u00E1', '\u00E2', '\u00E3',
        '\u00E4', '\u00E5', '\u00E6', '\u00E7', '\u00E8', '\u00E9',
        '\u00EA', '\u00EB', '\u00EC', '\u00ED', '\u00EE', '\u00EF',
        '\u00F0', '\u00F1', '\u00F2', '\u00F3', '\u00F4', '\u00F5',
        '\u00F6', '\u00F7', '\u00F8', '\u00F9', '\u00FA', '\u00FB',
        '\u00FC', '\u00FD', '\u00FE', '\u00FF'
    };

    /**
     * cp1253 to Unicode table
     * <pre>
     * Unicode version: 2.0
     * Table version: 2.01
     * Date: 04/15/98
     * URL:  http://www.unicode.org/Public/MAPPINGS/VENDORS/MICSFT/WINDOWS/CP1253.TXT
     * </pre>
     */
    private static char[] mappingTableCP1253 = {
        '\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005',
        '\u0006', '\u0007', '\u0008', '\u0009', '\n', '\u000B',
        '\u000C', '\r', '\u000E', '\u000F', '\u0010', '\u0011',
        '\u0012', '\u0013', '\u0014', '\u0015', '\u0016', '\u0017',
        '\u0018', '\u0019', '\u001A', '\u001B', '\u001C', '\u001D',
        '\u001E', '\u001F', '\u0020', '\u0021', '\u0022', '\u0023',
        '\u0024', '\u0025', '\u0026', '\'', '\u0028', '\u0029',
        '\u002A', '\u002B', '\u002C', '\u002D', '\u002E', '\u002F',
        '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035',
        '\u0036', '\u0037', '\u0038', '\u0039', '\u003A', '\u003B',
        '\u003C', '\u003D', '\u003E', '\u003F', '\u0040', '\u0041',
        '\u0042', '\u0043', '\u0044', '\u0045', '\u0046', '\u0047',
        '\u0048', '\u0049', '\u004A', '\u004B', '\u004C', '\u004D',
        '\u004E', '\u004F', '\u0050', '\u0051', '\u0052', '\u0053',
        '\u0054', '\u0055', '\u0056', '\u0057', '\u0058', '\u0059',
        '\u005A', '\u005B', '\\', '\u005D', '\u005E', '\u005F',
        '\u0060', '\u0061', '\u0062', '\u0063', '\u0064', '\u0065',
        '\u0066', '\u0067', '\u0068', '\u0069', '\u006A', '\u006B',
        '\u006C', '\u006D', '\u006E', '\u006F', '\u0070', '\u0071',
        '\u0072', '\u0073', '\u0074', '\u0075', '\u0076', '\u0077',
        '\u0078', '\u0079', '\u007A', '\u007B', '\u007C', '\u007D',
        '\u007E', '\u007F', '\u20AC', '\uFFFD', '\u201A', '\u0192',
        '\u201E', '\u2026', '\u2020', '\u2021', '\uFFFD', '\u2030',
        '\uFFFD', '\u2039', '\uFFFD', '\uFFFD', '\uFFFD', '\uFFFD',
        '\uFFFD', '\u2018', '\u2019', '\u201C', '\u201D', '\u2022',
        '\u2013', '\u2014', '\uFFFD', '\u2122', '\uFFFD', '\u203A',
        '\uFFFD', '\uFFFD', '\uFFFD', '\uFFFD', '\u00A0', '\u0385',
        '\u0386', '\u00A3', '\u00A4', '\u00A5', '\u00A6', '\u00A7',
        '\u00A8', '\u00A9', '\uFFFD', '\u00AB', '\u00AC', '\u00AD',
        '\u00AE', '\u2015', '\u00B0', '\u00B1', '\u00B2', '\u00B3',
        '\u0384', '\u00B5', '\u00B6', '\u00B7', '\u0388', '\u0389',
        '\u038A', '\u00BB', '\u038C', '\u00BD', '\u038E', '\u038F',
        '\u0390', '\u0391', '\u0392', '\u0393', '\u0394', '\u0395',
        '\u0396', '\u0397', '\u0398', '\u0399', '\u039A', '\u039B',
        '\u039C', '\u039D', '\u039E', '\u039F', '\u03A0', '\u03A1',
        '\uFFFD', '\u03A3', '\u03A4', '\u03A5', '\u03A6', '\u03A7',
        '\u03A8', '\u03A9', '\u03AA', '\u03AB', '\u03AC', '\u03AD',
        '\u03AE', '\u03AF', '\u03B0', '\u03B1', '\u03B2', '\u03B3',
        '\u03B4', '\u03B5', '\u03B6', '\u03B7', '\u03B8', '\u03B9',
        '\u03BA', '\u03BB', '\u03BC', '\u03BD', '\u03BE', '\u03BF',
        '\u03C0', '\u03C1', '\u03C2', '\u03C3', '\u03C4', '\u03C5',
        '\u03C6', '\u03C7', '\u03C8', '\u03C9', '\u03CA', '\u03CB',
        '\u03CC', '\u03CD', '\u03CE', '\uFFFD'
    };

    /**
     * cp1254 to Unicode table
     * <pre>
     * Unicode version: 2.0
     * Table version: 2.01
     * Date: 04/15/98
     * URL:  http://www.unicode.org/Public/MAPPINGS/VENDORS/MICSFT/WINDOWS/CP1254.TXT
     * </pre>
     */
    private static char[] mappingTableCP1254 = {
        '\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005',
        '\u0006', '\u0007', '\u0008', '\u0009', '\n', '\u000B',
        '\u000C', '\r', '\u000E', '\u000F', '\u0010', '\u0011',
        '\u0012', '\u0013', '\u0014', '\u0015', '\u0016', '\u0017',
        '\u0018', '\u0019', '\u001A', '\u001B', '\u001C', '\u001D',
        '\u001E', '\u001F', '\u0020', '\u0021', '\u0022', '\u0023',
        '\u0024', '\u0025', '\u0026', '\'', '\u0028', '\u0029',
        '\u002A', '\u002B', '\u002C', '\u002D', '\u002E', '\u002F',
        '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035',
        '\u0036', '\u0037', '\u0038', '\u0039', '\u003A', '\u003B',
        '\u003C', '\u003D', '\u003E', '\u003F', '\u0040', '\u0041',
        '\u0042', '\u0043', '\u0044', '\u0045', '\u0046', '\u0047',
        '\u0048', '\u0049', '\u004A', '\u004B', '\u004C', '\u004D',
        '\u004E', '\u004F', '\u0050', '\u0051', '\u0052', '\u0053',
        '\u0054', '\u0055', '\u0056', '\u0057', '\u0058', '\u0059',
        '\u005A', '\u005B', '\\', '\u005D', '\u005E', '\u005F',
        '\u0060', '\u0061', '\u0062', '\u0063', '\u0064', '\u0065',
        '\u0066', '\u0067', '\u0068', '\u0069', '\u006A', '\u006B',
        '\u006C', '\u006D', '\u006E', '\u006F', '\u0070', '\u0071',
        '\u0072', '\u0073', '\u0074', '\u0075', '\u0076', '\u0077',
        '\u0078', '\u0079', '\u007A', '\u007B', '\u007C', '\u007D',
        '\u007E', '\u007F', '\u20AC', '\uFFFD', '\u201A', '\u0192',
        '\u201E', '\u2026', '\u2020', '\u2021', '\u02C6', '\u2030',
        '\u0160', '\u2039', '\u0152', '\uFFFD', '\uFFFD', '\uFFFD',
        '\uFFFD', '\u2018', '\u2019', '\u201C', '\u201D', '\u2022',
        '\u2013', '\u2014', '\u02DC', '\u2122', '\u0161', '\u203A',
        '\u0153', '\uFFFD', '\uFFFD', '\u0178', '\u00A0', '\u00A1',
        '\u00A2', '\u00A3', '\u00A4', '\u00A5', '\u00A6', '\u00A7',
        '\u00A8', '\u00A9', '\u00AA', '\u00AB', '\u00AC', '\u00AD',
        '\u00AE', '\u00AF', '\u00B0', '\u00B1', '\u00B2', '\u00B3',
        '\u00B4', '\u00B5', '\u00B6', '\u00B7', '\u00B8', '\u00B9',
        '\u00BA', '\u00BB', '\u00BC', '\u00BD', '\u00BE', '\u00BF',
        '\u00C0', '\u00C1', '\u00C2', '\u00C3', '\u00C4', '\u00C5',
        '\u00C6', '\u00C7', '\u00C8', '\u00C9', '\u00CA', '\u00CB',
        '\u00CC', '\u00CD', '\u00CE', '\u00CF', '\u011E', '\u00D1',
        '\u00D2', '\u00D3', '\u00D4', '\u00D5', '\u00D6', '\u00D7',
        '\u00D8', '\u00D9', '\u00DA', '\u00DB', '\u00DC', '\u0130',
        '\u015E', '\u00DF', '\u00E0', '\u00E1', '\u00E2', '\u00E3',
        '\u00E4', '\u00E5', '\u00E6', '\u00E7', '\u00E8', '\u00E9',
        '\u00EA', '\u00EB', '\u00EC', '\u00ED', '\u00EE', '\u00EF',
        '\u011F', '\u00F1', '\u00F2', '\u00F3', '\u00F4', '\u00F5',
        '\u00F6', '\u00F7', '\u00F8', '\u00F9', '\u00FA', '\u00FB',
        '\u00FC', '\u0131', '\u015F', '\u00FF'
    };

    /**
     * cp1255 to Unicode table
     * <pre>
     * Unicode version: 2.0
     * Table version: 2.01
     * Date: 04/15/98
     * URL:  http://www.unicode.org/Public/MAPPINGS/VENDORS/MICSFT/WINDOWS/CP1255.TXT
     * </pre>
     */
    private static char[] mappingTableCP1255 = {
        '\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005',
        '\u0006', '\u0007', '\u0008', '\u0009', '\n', '\u000B',
        '\u000C', '\r', '\u000E', '\u000F', '\u0010', '\u0011',
        '\u0012', '\u0013', '\u0014', '\u0015', '\u0016', '\u0017',
        '\u0018', '\u0019', '\u001A', '\u001B', '\u001C', '\u001D',
        '\u001E', '\u001F', '\u0020', '\u0021', '\u0022', '\u0023',
        '\u0024', '\u0025', '\u0026', '\'', '\u0028', '\u0029',
        '\u002A', '\u002B', '\u002C', '\u002D', '\u002E', '\u002F',
        '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035',
        '\u0036', '\u0037', '\u0038', '\u0039', '\u003A', '\u003B',
        '\u003C', '\u003D', '\u003E', '\u003F', '\u0040', '\u0041',
        '\u0042', '\u0043', '\u0044', '\u0045', '\u0046', '\u0047',
        '\u0048', '\u0049', '\u004A', '\u004B', '\u004C', '\u004D',
        '\u004E', '\u004F', '\u0050', '\u0051', '\u0052', '\u0053',
        '\u0054', '\u0055', '\u0056', '\u0057', '\u0058', '\u0059',
        '\u005A', '\u005B', '\\', '\u005D', '\u005E', '\u005F',
        '\u0060', '\u0061', '\u0062', '\u0063', '\u0064', '\u0065',
        '\u0066', '\u0067', '\u0068', '\u0069', '\u006A', '\u006B',
        '\u006C', '\u006D', '\u006E', '\u006F', '\u0070', '\u0071',
        '\u0072', '\u0073', '\u0074', '\u0075', '\u0076', '\u0077',
        '\u0078', '\u0079', '\u007A', '\u007B', '\u007C', '\u007D',
        '\u007E', '\u007F', '\u20AC', '\uFFFD', '\u201A', '\u0192',
        '\u201E', '\u2026', '\u2020', '\u2021', '\u02C6', '\u2030',
        '\uFFFD', '\u2039', '\uFFFD', '\uFFFD', '\uFFFD', '\uFFFD',
        '\uFFFD', '\u2018', '\u2019', '\u201C', '\u201D', '\u2022',
        '\u2013', '\u2014', '\u02DC', '\u2122', '\uFFFD', '\u203A',
        '\uFFFD', '\uFFFD', '\uFFFD', '\uFFFD', '\u00A0', '\u00A1',
        '\u00A2', '\u00A3', '\u20AA', '\u00A5', '\u00A6', '\u00A7',
        '\u00A8', '\u00A9', '\u00D7', '\u00AB', '\u00AC', '\u00AD',
        '\u00AE', '\u00AF', '\u00B0', '\u00B1', '\u00B2', '\u00B3',
        '\u00B4', '\u00B5', '\u00B6', '\u00B7', '\u00B8', '\u00B9',
        '\u00F7', '\u00BB', '\u00BC', '\u00BD', '\u00BE', '\u00BF',
        '\u05B0', '\u05B1', '\u05B2', '\u05B3', '\u05B4', '\u05B5',
        '\u05B6', '\u05B7', '\u05B8', '\u05B9', '\uFFFD', '\u05BB',
        '\u05BC', '\u05BD', '\u05BE', '\u05BF', '\u05C0', '\u05C1',
        '\u05C2', '\u05C3', '\u05F0', '\u05F1', '\u05F2', '\u05F3',
        '\u05F4', '\uFFFD', '\uFFFD', '\uFFFD', '\uFFFD', '\uFFFD',
        '\uFFFD', '\uFFFD', '\u05D0', '\u05D1', '\u05D2', '\u05D3',
        '\u05D4', '\u05D5', '\u05D6', '\u05D7', '\u05D8', '\u05D9',
        '\u05DA', '\u05DB', '\u05DC', '\u05DD', '\u05DE', '\u05DF',
        '\u05E0', '\u05E1', '\u05E2', '\u05E3', '\u05E4', '\u05E5',
        '\u05E6', '\u05E7', '\u05E8', '\u05E9', '\u05EA', '\uFFFD',
        '\uFFFD', '\u200E', '\u200F', '\uFFFD'
    };

    /**
     * cp1256 to Unicode table
     * <pre>
     * Unicode version: 2.0
     * Table version: 2.01
     * Date: 04/15/98
     * URL:  http://www.unicode.org/Public/MAPPINGS/VENDORS/MICSFT/WINDOWS/CP1256.TXT
     * </pre>
     */
    private static char[] mappingTableCP1256 = {
        '\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005',
        '\u0006', '\u0007', '\u0008', '\u0009', '\n', '\u000B',
        '\u000C', '\r', '\u000E', '\u000F', '\u0010', '\u0011',
        '\u0012', '\u0013', '\u0014', '\u0015', '\u0016', '\u0017',
        '\u0018', '\u0019', '\u001A', '\u001B', '\u001C', '\u001D',
        '\u001E', '\u001F', '\u0020', '\u0021', '\u0022', '\u0023',
        '\u0024', '\u0025', '\u0026', '\'', '\u0028', '\u0029',
        '\u002A', '\u002B', '\u002C', '\u002D', '\u002E', '\u002F',
        '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035',
        '\u0036', '\u0037', '\u0038', '\u0039', '\u003A', '\u003B',
        '\u003C', '\u003D', '\u003E', '\u003F', '\u0040', '\u0041',
        '\u0042', '\u0043', '\u0044', '\u0045', '\u0046', '\u0047',
        '\u0048', '\u0049', '\u004A', '\u004B', '\u004C', '\u004D',
        '\u004E', '\u004F', '\u0050', '\u0051', '\u0052', '\u0053',
        '\u0054', '\u0055', '\u0056', '\u0057', '\u0058', '\u0059',
        '\u005A', '\u005B', '\\', '\u005D', '\u005E', '\u005F',
        '\u0060', '\u0061', '\u0062', '\u0063', '\u0064', '\u0065',
        '\u0066', '\u0067', '\u0068', '\u0069', '\u006A', '\u006B',
        '\u006C', '\u006D', '\u006E', '\u006F', '\u0070', '\u0071',
        '\u0072', '\u0073', '\u0074', '\u0075', '\u0076', '\u0077',
        '\u0078', '\u0079', '\u007A', '\u007B', '\u007C', '\u007D',
        '\u007E', '\u007F', '\u20AC', '\u067E', '\u201A', '\u0192',
        '\u201E', '\u2026', '\u2020', '\u2021', '\u02C6', '\u2030',
        '\u0679', '\u2039', '\u0152', '\u0686', '\u0698', '\u0688',
        '\u06AF', '\u2018', '\u2019', '\u201C', '\u201D', '\u2022',
        '\u2013', '\u2014', '\u06A9', '\u2122', '\u0691', '\u203A',
        '\u0153', '\u200C', '\u200D', '\u06BA', '\u00A0', '\u060C',
        '\u00A2', '\u00A3', '\u00A4', '\u00A5', '\u00A6', '\u00A7',
        '\u00A8', '\u00A9', '\u06BE', '\u00AB', '\u00AC', '\u00AD',
        '\u00AE', '\u00AF', '\u00B0', '\u00B1', '\u00B2', '\u00B3',
        '\u00B4', '\u00B5', '\u00B6', '\u00B7', '\u00B8', '\u00B9',
        '\u061B', '\u00BB', '\u00BC', '\u00BD', '\u00BE', '\u061F',
        '\u06C1', '\u0621', '\u0622', '\u0623', '\u0624', '\u0625',
        '\u0626', '\u0627', '\u0628', '\u0629', '\u062A', '\u062B',
        '\u062C', '\u062D', '\u062E', '\u062F', '\u0630', '\u0631',
        '\u0632', '\u0633', '\u0634', '\u0635', '\u0636', '\u00D7',
        '\u0637', '\u0638', '\u0639', '\u063A', '\u0640', '\u0641',
        '\u0642', '\u0643', '\u00E0', '\u0644', '\u00E2', '\u0645',
        '\u0646', '\u0647', '\u0648', '\u00E7', '\u00E8', '\u00E9',
        '\u00EA', '\u00EB', '\u0649', '\u064A', '\u00EE', '\u00EF',
        '\u064B', '\u064C', '\u064D', '\u064E', '\u00F4', '\u064F',
        '\u0650', '\u00F7', '\u0651', '\u00F9', '\u0652', '\u00FB',
        '\u00FC', '\u200E', '\u200F', '\u06D2'
    };

    /**
     * cp1257 to Unicode table
     * <pre>
     * Unicode version: 2.0
     * Table version: 2.01
     * Date: 04/15/98
     * URL:  http://www.unicode.org/Public/MAPPINGS/VENDORS/MICSFT/WINDOWS/CP1257.TXT
     * </pre>
     */
    private static char[] mappingTableCP1257 = {
        '\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005',
        '\u0006', '\u0007', '\u0008', '\u0009', '\n', '\u000B',
        '\u000C', '\r', '\u000E', '\u000F', '\u0010', '\u0011',
        '\u0012', '\u0013', '\u0014', '\u0015', '\u0016', '\u0017',
        '\u0018', '\u0019', '\u001A', '\u001B', '\u001C', '\u001D',
        '\u001E', '\u001F', '\u0020', '\u0021', '\u0022', '\u0023',
        '\u0024', '\u0025', '\u0026', '\'', '\u0028', '\u0029',
        '\u002A', '\u002B', '\u002C', '\u002D', '\u002E', '\u002F',
        '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035',
        '\u0036', '\u0037', '\u0038', '\u0039', '\u003A', '\u003B',
        '\u003C', '\u003D', '\u003E', '\u003F', '\u0040', '\u0041',
        '\u0042', '\u0043', '\u0044', '\u0045', '\u0046', '\u0047',
        '\u0048', '\u0049', '\u004A', '\u004B', '\u004C', '\u004D',
        '\u004E', '\u004F', '\u0050', '\u0051', '\u0052', '\u0053',
        '\u0054', '\u0055', '\u0056', '\u0057', '\u0058', '\u0059',
        '\u005A', '\u005B', '\\', '\u005D', '\u005E', '\u005F',
        '\u0060', '\u0061', '\u0062', '\u0063', '\u0064', '\u0065',
        '\u0066', '\u0067', '\u0068', '\u0069', '\u006A', '\u006B',
        '\u006C', '\u006D', '\u006E', '\u006F', '\u0070', '\u0071',
        '\u0072', '\u0073', '\u0074', '\u0075', '\u0076', '\u0077',
        '\u0078', '\u0079', '\u007A', '\u007B', '\u007C', '\u007D',
        '\u007E', '\u007F', '\u20AC', '\uFFFD', '\u201A', '\uFFFD',
        '\u201E', '\u2026', '\u2020', '\u2021', '\uFFFD', '\u2030',
        '\uFFFD', '\u2039', '\uFFFD', '\u00A8', '\u02C7', '\u00B8',
        '\uFFFD', '\u2018', '\u2019', '\u201C', '\u201D', '\u2022',
        '\u2013', '\u2014', '\uFFFD', '\u2122', '\uFFFD', '\u203A',
        '\uFFFD', '\u00AF', '\u02DB', '\uFFFD', '\u00A0', '\uFFFD',
        '\u00A2', '\u00A3', '\u00A4', '\uFFFD', '\u00A6', '\u00A7',
        '\u00D8', '\u00A9', '\u0156', '\u00AB', '\u00AC', '\u00AD',
        '\u00AE', '\u00C6', '\u00B0', '\u00B1', '\u00B2', '\u00B3',
        '\u00B4', '\u00B5', '\u00B6', '\u00B7', '\u00F8', '\u00B9',
        '\u0157', '\u00BB', '\u00BC', '\u00BD', '\u00BE', '\u00E6',
        '\u0104', '\u012E', '\u0100', '\u0106', '\u00C4', '\u00C5',
        '\u0118', '\u0112', '\u010C', '\u00C9', '\u0179', '\u0116',
        '\u0122', '\u0136', '\u012A', '\u013B', '\u0160', '\u0143',
        '\u0145', '\u00D3', '\u014C', '\u00D5', '\u00D6', '\u00D7',
        '\u0172', '\u0141', '\u015A', '\u016A', '\u00DC', '\u017B',
        '\u017D', '\u00DF', '\u0105', '\u012F', '\u0101', '\u0107',
        '\u00E4', '\u00E5', '\u0119', '\u0113', '\u010D', '\u00E9',
        '\u017A', '\u0117', '\u0123', '\u0137', '\u012B', '\u013C',
        '\u0161', '\u0144', '\u0146', '\u00F3', '\u014D', '\u00F5',
        '\u00F6', '\u00F7', '\u0173', '\u0142', '\u015B', '\u016B',
        '\u00FC', '\u017C', '\u017E', '\u02D9'
    };

    /**
     * cp1258 to Unicode table
     * <pre>
     * Unicode version: 2.0
     * Table version: 2.01
     * Date: 04/15/98
     * URL:  http://www.unicode.org/Public/MAPPINGS/VENDORS/MICSFT/WINDOWS/CP1258.TXT
     * </pre>
     */
    private static char[] mappingTableCP1258 = {
        '\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005',
        '\u0006', '\u0007', '\u0008', '\u0009', '\n', '\u000B',
        '\u000C', '\r', '\u000E', '\u000F', '\u0010', '\u0011',
        '\u0012', '\u0013', '\u0014', '\u0015', '\u0016', '\u0017',
        '\u0018', '\u0019', '\u001A', '\u001B', '\u001C', '\u001D',
        '\u001E', '\u001F', '\u0020', '\u0021', '\u0022', '\u0023',
        '\u0024', '\u0025', '\u0026', '\'', '\u0028', '\u0029',
        '\u002A', '\u002B', '\u002C', '\u002D', '\u002E', '\u002F',
        '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035',
        '\u0036', '\u0037', '\u0038', '\u0039', '\u003A', '\u003B',
        '\u003C', '\u003D', '\u003E', '\u003F', '\u0040', '\u0041',
        '\u0042', '\u0043', '\u0044', '\u0045', '\u0046', '\u0047',
        '\u0048', '\u0049', '\u004A', '\u004B', '\u004C', '\u004D',
        '\u004E', '\u004F', '\u0050', '\u0051', '\u0052', '\u0053',
        '\u0054', '\u0055', '\u0056', '\u0057', '\u0058', '\u0059',
        '\u005A', '\u005B', '\\', '\u005D', '\u005E', '\u005F',
        '\u0060', '\u0061', '\u0062', '\u0063', '\u0064', '\u0065',
        '\u0066', '\u0067', '\u0068', '\u0069', '\u006A', '\u006B',
        '\u006C', '\u006D', '\u006E', '\u006F', '\u0070', '\u0071',
        '\u0072', '\u0073', '\u0074', '\u0075', '\u0076', '\u0077',
        '\u0078', '\u0079', '\u007A', '\u007B', '\u007C', '\u007D',
        '\u007E', '\u007F', '\u20AC', '\uFFFD', '\u201A', '\u0192',
        '\u201E', '\u2026', '\u2020', '\u2021', '\u02C6', '\u2030',
        '\uFFFD', '\u2039', '\u0152', '\uFFFD', '\uFFFD', '\uFFFD',
        '\uFFFD', '\u2018', '\u2019', '\u201C', '\u201D', '\u2022',
        '\u2013', '\u2014', '\u02DC', '\u2122', '\uFFFD', '\u203A',
        '\u0153', '\uFFFD', '\uFFFD', '\u0178', '\u00A0', '\u00A1',
        '\u00A2', '\u00A3', '\u00A4', '\u00A5', '\u00A6', '\u00A7',
        '\u00A8', '\u00A9', '\u00AA', '\u00AB', '\u00AC', '\u00AD',
        '\u00AE', '\u00AF', '\u00B0', '\u00B1', '\u00B2', '\u00B3',
        '\u00B4', '\u00B5', '\u00B6', '\u00B7', '\u00B8', '\u00B9',
        '\u00BA', '\u00BB', '\u00BC', '\u00BD', '\u00BE', '\u00BF',
        '\u00C0', '\u00C1', '\u00C2', '\u0102', '\u00C4', '\u00C5',
        '\u00C6', '\u00C7', '\u00C8', '\u00C9', '\u00CA', '\u00CB',
        '\u0300', '\u00CD', '\u00CE', '\u00CF', '\u0110', '\u00D1',
        '\u0309', '\u00D3', '\u00D4', '\u01A0', '\u00D6', '\u00D7',
        '\u00D8', '\u00D9', '\u00DA', '\u00DB', '\u00DC', '\u01AF',
        '\u0303', '\u00DF', '\u00E0', '\u00E1', '\u00E2', '\u0103',
        '\u00E4', '\u00E5', '\u00E6', '\u00E7', '\u00E8', '\u00E9',
        '\u00EA', '\u00EB', '\u0301', '\u00ED', '\u00EE', '\u00EF',
        '\u0111', '\u00F1', '\u0323', '\u00F3', '\u00F4', '\u01A1',
        '\u00F6', '\u00F7', '\u00F8', '\u00F9', '\u00FA', '\u00FB',
        '\u00FC', '\u01B0', '\u20AB', '\u00FF'
    };
}
