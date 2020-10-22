/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.api.response;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.gson.Gson;
import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.sdkws.api.response.GruwareResponse.GruwareFirmwareResponse;
import com.kolibree.sdkws.api.response.GruwareResponse.GruwareGruResponse;
import org.junit.Test;

/** Created by miguelaragues on 4/5/18. */
public class GruwareResponseTest extends BaseUnitTest {

  public static final String GRU_UPDATE_URL =
      "https://kolibree-firmware.s3.amazonaws.com/prod/fw/GRU_DATA_0x0B020000_058345fb.bin?AWSAccessKeyId=ASIAJ2A7CCKUL34VHVFQ&Expires=1525764687&x-amz-security-token=FQoDYXdzELj%2F%2F%2F%2F%2F%2F%2F%2F%2F%2FwEaDJIp5LNFyPj3qRZWjSK3A9cJJPtudMVXXXFlwdKIuQ2rc7T9m6naYGm6VG2zyMDreEu4Bi91KT94K%2BntyCg59ONqf9XN3MaUy4D2fZ9lUx%2FBfB8YM4ozQ5kyZAjXHFST%2BZ82ikjK542ojRyTfaVgvC5CVz4tV9p4kYtpq2hoj1yuAT27dt%2BajMS05Z8%2BhXaiaWfeg31eEKEp5ke%2FQR2TcyfSZ4CrehXUaNFH7Z0QpnINs5krTh4%2BJNxkAZSry5tt1oxOHRZQEFT2LC3FEhBl6Ilk0f108PFNz6BvxqdgzstrpjRXMFjHZra3H6f3OVU9rZ5NsetqqJw3%2FmzMBwzYkgw3encKRTg4qwhoNx90r2%2Bmz89VH41qZ1K529uVxMWCHEdHlwZy%2F9Etg%2FOxrgXdr0L4sh%2BgSMQqpeUsoslWy2LBSVLdZY4fapcNNXGOJ%2BDEhm8ZRSJXzYA43JXD4N6gGPsSty3yn6E9DDe%2FmuWcGyj0qGFOkhGOBPsNwEq2GK7LlUKRtdGvfOk0%2F9tfM5SZzQebKum4adpL%2BUsB34DzsXigntCmksCBd9ZgpLJ3ZsSwsknUnkTUOnbN%2BxrvrXUoqu7Uqf8RQn0oxpTF1wU%3D&Signature=W%2BZwRgC0r6S9TGRYSHik92bPqic%3D";
  public static final String FW_UPDATE_URL =
      "https://kolibree-firmware.s3.amazonaws.com/prod/fw/KLTB002_HW_0x00020004_FW_0x010F0000_9d752091.bin?AWSAccessKeyId=ASIAJ2A7CCKUL34VHVFQ&Expires=1525764687&x-amz-security-token=FQoDYXdzELj%2F%2F%2F%2F%2F%2F%2F%2F%2F%2FwEaDJIp5LNFyPj3qRZWjSK3A9cJJPtudMVXXXFlwdKIuQ2rc7T9m6naYGm6VG2zyMDreEu4Bi91KT94K%2BntyCg59ONqf9XN3MaUy4D2fZ9lUx%2FBfB8YM4ozQ5kyZAjXHFST%2BZ82ikjK542ojRyTfaVgvC5CVz4tV9p4kYtpq2hoj1yuAT27dt%2BajMS05Z8%2BhXaiaWfeg31eEKEp5ke%2FQR2TcyfSZ4CrehXUaNFH7Z0QpnINs5krTh4%2BJNxkAZSry5tt1oxOHRZQEFT2LC3FEhBl6Ilk0f108PFNz6BvxqdgzstrpjRXMFjHZra3H6f3OVU9rZ5NsetqqJw3%2FmzMBwzYkgw3encKRTg4qwhoNx90r2%2Bmz89VH41qZ1K529uVxMWCHEdHlwZy%2F9Etg%2FOxrgXdr0L4sh%2BgSMQqpeUsoslWy2LBSVLdZY4fapcNNXGOJ%2BDEhm8ZRSJXzYA43JXD4N6gGPsSty3yn6E9DDe%2FmuWcGyj0qGFOkhGOBPsNwEq2GK7LlUKRtdGvfOk0%2F9tfM5SZzQebKum4adpL%2BUsB34DzsXigntCmksCBd9ZgpLJ3ZsSwsknUnkTUOnbN%2BxrvrXUoqu7Uqf8RQn0oxpTF1wU%3D&Signature=%2FEwPONz7OgqScUf5zROHZkwN4eI%3D";
  public static final String GRU_VERSION = "11.2.0";
  public static final String FW_VERSION = "1.14.0";
  public static final String GRU_FILENAME = "GRU_DATA_0x0B020000_058345fb.bin";
  public static final String FW_FILENAME = "KLTB002_HW_0x00020004_FW_0x010F0000_9d752091.bin";
  /*
    {
  	"gru": {
  		"gru": "11.2.0",
  		"crc16": "",
  		"filename": "GRU_DATA_0x0B020000_058345fb.bin",
  		"beta": true,
  		"link": "https://kolibree-firmware.s3.amazonaws.com/prod/fw/GRU_DATA_0x0B020000_058345fb.bin?AWSAccessKeyId=ASIAJ2A7CCKUL34VHVFQ&Expires=1525764687&x-amz-security-token=FQoDYXdzELj%2F%2F%2F%2F%2F%2F%2F%2F%2F%2FwEaDJIp5LNFyPj3qRZWjSK3A9cJJPtudMVXXXFlwdKIuQ2rc7T9m6naYGm6VG2zyMDreEu4Bi91KT94K%2BntyCg59ONqf9XN3MaUy4D2fZ9lUx%2FBfB8YM4ozQ5kyZAjXHFST%2BZ82ikjK542ojRyTfaVgvC5CVz4tV9p4kYtpq2hoj1yuAT27dt%2BajMS05Z8%2BhXaiaWfeg31eEKEp5ke%2FQR2TcyfSZ4CrehXUaNFH7Z0QpnINs5krTh4%2BJNxkAZSry5tt1oxOHRZQEFT2LC3FEhBl6Ilk0f108PFNz6BvxqdgzstrpjRXMFjHZra3H6f3OVU9rZ5NsetqqJw3%2FmzMBwzYkgw3encKRTg4qwhoNx90r2%2Bmz89VH41qZ1K529uVxMWCHEdHlwZy%2F9Etg%2FOxrgXdr0L4sh%2BgSMQqpeUsoslWy2LBSVLdZY4fapcNNXGOJ%2BDEhm8ZRSJXzYA43JXD4N6gGPsSty3yn6E9DDe%2FmuWcGyj0qGFOkhGOBPsNwEq2GK7LlUKRtdGvfOk0%2F9tfM5SZzQebKum4adpL%2BUsB34DzsXigntCmksCBd9ZgpLJ3ZsSwsknUnkTUOnbN%2BxrvrXUoqu7Uqf8RQn0oxpTF1wU%3D&Signature=W%2BZwRgC0r6S9TGRYSHik92bPqic%3D",
  		"crc32": "058345fb"
  	},
  	"fw": {
  		"crc16": "",
  		"fw": "1.15.0",
  		"filename": "KLTB002_HW_0x00020004_FW_0x010F0000_9d752091.bin",
  		"beta": true,
  		"link": "https://kolibree-firmware.s3.amazonaws.com/prod/fw/KLTB002_HW_0x00020004_FW_0x010F0000_9d752091.bin?AWSAccessKeyId=ASIAJ2A7CCKUL34VHVFQ&Expires=1525764687&x-amz-security-token=FQoDYXdzELj%2F%2F%2F%2F%2F%2F%2F%2F%2F%2FwEaDJIp5LNFyPj3qRZWjSK3A9cJJPtudMVXXXFlwdKIuQ2rc7T9m6naYGm6VG2zyMDreEu4Bi91KT94K%2BntyCg59ONqf9XN3MaUy4D2fZ9lUx%2FBfB8YM4ozQ5kyZAjXHFST%2BZ82ikjK542ojRyTfaVgvC5CVz4tV9p4kYtpq2hoj1yuAT27dt%2BajMS05Z8%2BhXaiaWfeg31eEKEp5ke%2FQR2TcyfSZ4CrehXUaNFH7Z0QpnINs5krTh4%2BJNxkAZSry5tt1oxOHRZQEFT2LC3FEhBl6Ilk0f108PFNz6BvxqdgzstrpjRXMFjHZra3H6f3OVU9rZ5NsetqqJw3%2FmzMBwzYkgw3encKRTg4qwhoNx90r2%2Bmz89VH41qZ1K529uVxMWCHEdHlwZy%2F9Etg%2FOxrgXdr0L4sh%2BgSMQqpeUsoslWy2LBSVLdZY4fapcNNXGOJ%2BDEhm8ZRSJXzYA43JXD4N6gGPsSty3yn6E9DDe%2FmuWcGyj0qGFOkhGOBPsNwEq2GK7LlUKRtdGvfOk0%2F9tfM5SZzQebKum4adpL%2BUsB34DzsXigntCmksCBd9ZgpLJ3ZsSwsknUnkTUOnbN%2BxrvrXUoqu7Uqf8RQn0oxpTF1wU%3D&Signature=%2FEwPONz7OgqScUf5zROHZkwN4eI%3D",
  		"crc32": "9d752091"
  	}
  }
     */
  public static final String GRUWARE_RESPONSE =
      "{\n"
          + "\t\"gru\": {\n"
          + "\t\t\"gru\": \""
          + GRU_VERSION
          + "\",\n"
          + "\t\t\"crc16\": \"\",\n"
          + "\t\t\"filename\": \""
          + GRU_FILENAME
          + "\",\n"
          + "\t\t\"beta\": true,\n"
          + "\t\t\"link\": \""
          + GRU_UPDATE_URL
          + "\",\n"
          + "\t\t\"crc32\": \"058345fb\"\n"
          + "\t},\n"
          + "\t\"fw\": {\n"
          + "\t\t\"crc16\": \"\",\n"
          + "\t\t\"fw\": \""
          + FW_VERSION
          + "\",\n"
          + "\t\t\"filename\": \""
          + FW_FILENAME
          + "\",\n"
          + "\t\t\"beta\": true,\n"
          + "\t\t\"link\": \""
          + FW_UPDATE_URL
          + "\",\n"
          + "\t\t\"crc32\": \"null\"\n"
          + "\t}\n"
          + "}";

  @Test
  public void parse() {
    GruwareResponse gruwareResponse = new Gson().fromJson(GRUWARE_RESPONSE, GruwareResponse.class);

    GruwareFirmwareResponse firmware = gruwareResponse.firmware();
    GruwareGruResponse gru = gruwareResponse.gru();

    assertNotNull(firmware);
    assertNotNull(gru);

    assertEquals(GRU_VERSION, gru.getDataVersion());
    assertEquals(GRU_UPDATE_URL, gru.getLink());

    assertEquals(FW_VERSION, firmware.getFirmwareVersion());
    assertEquals(FW_UPDATE_URL, firmware.getLink());
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void parse_crc32_nullIfNotPresentOrNullString() {
    GruwareResponse gruwareResponse = new Gson().fromJson(GRUWARE_RESPONSE, GruwareResponse.class);

    assertEquals("058345fb", gruwareResponse.gru().getCrc32());
    assertNull(gruwareResponse.firmware().getCrc32());
  }
}
