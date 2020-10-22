/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.magiclink;

import static com.kolibree.android.app.test.mocks.MocksUtils.mockUri;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.net.Uri;
import com.kolibree.android.failearly.FailEarly;
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/** {@link MagicLinkParser} unit tests */
public class MagicLinkParserTest {

  private MagicLinkParser magicLinkParser;

  @Before
  public void init() {
    FailEarly.overrideDelegateWith(NoopTestDelegate.INSTANCE);
    magicLinkParser = new MagicLinkParser();
  }

  @Test
  public void valueOf_kolibreeCompatSchemeIs_kolibree() {
    assertEquals("kolibree", MagicLinkParser.SCHEME_KOLIBREE_COMPAT);
  }

  @Test
  public void valueOf_colgateCompatSchemeIs_colgateconnect() {
    assertEquals("colgateconnect", MagicLinkParser.SCHEME_COLGATE_COMPAT);
  }

  @Test
  public void valueOf_humCompatSchemeIs_colgatehum() {
    assertEquals("colgatehum", MagicLinkParser.SCHEME_HUM_COMPAT);
  }

  @Test
  public void testHost() {
    assertEquals("kolibree.com", MagicLinkParser.HOST_SUFFIX);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testParseCompatKolibreeLink() {
    final Uri magicLink = mockUri("kolibree://", "auth/magiclink/JE-SUIS-UN-CODE", null);
    assertEquals("JE-SUIS-UN-CODE", magicLinkParser.parse(magicLink));
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testParseCompatColgateConnectLink() {
    final Uri magicLink = mockUri("colgateconnect://", "auth/magiclink/SOY-UN-CODIGO", null);
    assertEquals("SOY-UN-CODIGO", magicLinkParser.parse(magicLink));
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testParseCompatHumLink() {
    final Uri magicLink = mockUri("colgatehum://", "auth/magiclink/SOY-UN-CODIGO", null);
    assertEquals("SOY-UN-CODIGO", magicLinkParser.parse(magicLink));
  }

  @SuppressWarnings("deprecation")
  @Test(expected = IllegalStateException.class)
  public void testParseUnknownSchemeThrowsException() {
    magicLinkParser.parse(mockUri("oralb://", "auth/magiclink/IM-A-CODE", null));
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testParseKolibreeLink() {
    final Map<String, String> parameters = new HashMap<>();
    parameters.put("app", "kolibree");
    parameters.put("code", "JE-SUIS-UN-CODE");

    final Uri magicLink = mockUri("https://", "kolibree.com", parameters);
    assertEquals("JE-SUIS-UN-CODE", magicLinkParser.parse(magicLink));
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testParseColgateConnectLink() {
    final Map<String, String> parameters = new HashMap<>();
    parameters.put("app", "colgateconnect");
    parameters.put("code", "JE-SUIS-UN-CODE");

    final Uri magicLink = mockUri("https://", "kolibree.com", parameters);
    assertEquals("JE-SUIS-UN-CODE", magicLinkParser.parse(magicLink));
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testParseHumLink() {
    final Map<String, String> parameters = new HashMap<>();
    parameters.put("app", "colgatehum");
    parameters.put("code", "JE-SUIS-UN-CODE");

    final Uri magicLink = mockUri("https://", "kolibree.com", parameters);
    assertEquals("JE-SUIS-UN-CODE", magicLinkParser.parse(magicLink));
  }

  @SuppressWarnings("deprecation")
  @Test(expected = IllegalStateException.class)
  public void testParseUnknownHostThrowsException() {
    final Map<String, String> parameters = new HashMap<>();
    parameters.put("app", "colgateconnect");
    parameters.put("code", "JE-SUIS-UN-CODE");

    final Uri magicLink = mockUri("https://", "oralb.com", parameters);
    magicLinkParser.parse(magicLink);
  }

  @SuppressWarnings("deprecation")
  @Test(expected = IllegalStateException.class)
  public void testParseParameterLessLinkThrowsException() {
    final Map<String, String> parameters = new HashMap<>();
    final Uri magicLink = mockUri("https://", "kolibree.com", parameters);
    magicLinkParser.parse(magicLink);
  }

  @SuppressWarnings("deprecation")
  @Test(expected = IllegalStateException.class)
  public void testParseParameterLessCompatLinkThrowsException() {
    final Uri magicLink = mockUri("kolibree://", "auth/magiclink/", null);
    magicLinkParser.parse(magicLink);
  }

  /*
  parseMagicCode
   */

  @Test
  public void parseMagicCode_returnsExpectedMagicCode_forCompatColgateUri() {
    final String expectedCode = "CODE;code";
    final Uri magicLink = mockUri("colgateconnect://", "auth/magiclink/" + expectedCode, null);

    final MagicCode code = magicLinkParser.parseMagicCode(magicLink);

    assertEquals(expectedCode, code.getCode());
    assertTrue(code.getAlreadyValidated());
  }

  @Test
  public void parseMagicCode_returnsExpectedMagicCode_forCompatKolibreeUri() {
    final String expectedCode = "CODE;code";
    final Uri magicLink = mockUri("kolibree://", "auth/magiclink/" + expectedCode, null);

    final MagicCode code = magicLinkParser.parseMagicCode(magicLink);

    assertEquals(expectedCode, code.getCode());
    assertTrue(code.getAlreadyValidated());
  }

  @Test
  public void parseMagicCode_returnsExpectedMagicCode_forCompatHumUri() {
    final String expectedCode = "CODE;code";
    final Uri magicLink = mockUri("colgatehum://", "auth/magiclink/" + expectedCode, null);

    final MagicCode code = magicLinkParser.parseMagicCode(magicLink);

    assertEquals(expectedCode, code.getCode());
    assertTrue(code.getAlreadyValidated());
  }

  @Test
  public void parseMagicCode_returnsExpectedMagicCode_forColgateUri() {
    final String expectedCode = "CODE;code";
    final Map<String, String> parameters = new HashMap<>();
    parameters.put("app", "colgateconnect");
    parameters.put("code", expectedCode);

    final Uri magicLink = mockUri("https://", "kolibree.com", parameters);
    final MagicCode code = magicLinkParser.parseMagicCode(magicLink);

    assertEquals(expectedCode, code.getCode());
    assertFalse(code.getAlreadyValidated());
  }

  @Test
  public void parseMagicCode_returnsExpectedMagicCode_forKolibreeUri() {
    final String expectedCode = "CODE;code";
    final Map<String, String> parameters = new HashMap<>();
    parameters.put("app", "kolibree");
    parameters.put("code", expectedCode);

    final Uri magicLink = mockUri("https://", "kolibree.com", parameters);
    final MagicCode code = magicLinkParser.parseMagicCode(magicLink);

    assertEquals(expectedCode, code.getCode());
    assertFalse(code.getAlreadyValidated());
  }

  @Test
  public void parseMagicCode_returnsExpectedMagicCode_forHumUri() {
    final String expectedCode = "CODE;code";
    final Map<String, String> parameters = new HashMap<>();
    parameters.put("app", "colgatehum");
    parameters.put("code", expectedCode);

    final Uri magicLink = mockUri("https://", "kolibree.com", parameters);
    final MagicCode code = magicLinkParser.parseMagicCode(magicLink);

    assertEquals(expectedCode, code.getCode());
    assertFalse(code.getAlreadyValidated());
  }
}
