/**
 * Provides all core classes and method to do extraction and listing of mail
 * boxes.
 * <p>
 * It uses:
 * <ul>
 * <li>JavaMail to access IMAP, IMAPS, POP3 (GIMAP experimental) account and Thunderbird mbox
 * directory or Eml,</li>and
 * <li>libpst to access Outlook pst files,</li>
 * <li>apache POI HSMF to access msg files.</li>
 * </ul>
 */
package fr.gouv.vitam.tools.mailextract.lib.core;