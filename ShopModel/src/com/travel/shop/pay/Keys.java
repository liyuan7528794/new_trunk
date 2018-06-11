/*
 * Copyright (C) 2010 The MobileSecurePay Project
 * All right reserved.
 * author: shiqun.shi@alipay.com
 * 
 *  提示：如何获取安全校验码和合作身份者id
 *  1.用您的签约支付宝账号登录支付宝网站(www.alipay.com)
 *  2.点击“商家服务”(https://b.alipay.com/order/myorder.htm)
 *  3.点击“查询合作者身份(pid)”、“查询安全校验码(key)”
 */

package com.travel.shop.pay;

//
// 请参考 Android平台安全支付服务(msp)应用开发接口(4.2 RSA算法签名)部分，并使用压缩包中的openssl RSA密钥生成工具，生成一套RSA公私钥。
// 这里签名时，只需要使用生成的RSA私钥。
// Note: 为安全起见，使用RSA私钥进行签名的操作过程，应该尽量放到商家服务器端去进行。
public final class Keys {

	// 合作身份者id，以2088开头的16位纯数字
	public static String DEFAULT_PARTNER = "2088221543404188";
	// 收款支付宝账号
	public static String DEFAULT_SELLER = "zhoutong@cts-im.com";
	// 商户密钥，自助生成
	public static String PRIVATE = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKgQnA+iqiXlOZtdUqbjEvHbBQiWHhAkaHjP+KsF9370hABa3EL6RVLsmpOii6rQmvJBbMIdmF1hZIxXXSpD3bl501Eqln1O5mHDzAqpivsdvpKdNWnXKsxPWrr/rQ7Afw80Sa46uPiyDaeKAci0MO0RGm9VJH9OSy3NZv3zte7NAgMBAAECgYBF/lTgdiBik+q/98faVSuoqkyYXNy8YE1aM1Me9ucyamcnNrAQ/OYuOPfPkJyWUTaWugSve1/fVBZqFGBqn8URMwfd+Ovw1kAoI4OqGB2wy+091vAO11g19fw4fj66nPXcCL40KAFBBUgCV6ciuF6QH0vQsqO+tnCEHsmvnfURCQJBANRClMpgD6z2T9XbZvm1KI5YhKqftA4k120jocSiLWtT+mKcBUbQrgbuRXMEDeoxrEVcaHbRqBe//kEhYp8628sCQQDKspX89gz3mXUcG2TnToa0G+wa74wvHfSZtg2O8E9RX+WBA4KW2dtLAxk7818QSuvNL/LPpm9+lZdwrf7BJLzHAkAKhdZ8hfChAYEJuBvNy1ZcYDULhGlXvinT5k1Rwzx/MqWaF+QHE7dc7pkQz6Wk85t3wNII+fHcY49GSVJrVG6rAkAFNu7w4QuYWxROMs43vSdt/sHmN39tvuGKCsrygHAp8cOfvA1iABzKIzxE9I/fhW3ni3I9sVRw9zw8I93H7kAFAkBQKlFu3I18cT2qBAfm7KQ1mxi6xtPNv0mU3cnLKUTkwKZN81Cg0xRxuP9YsnrWFMYXXn/RTIIqDlrxCM2vzHMz";
	public static String PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";

	public void setValue(String partner, String seller, String str_private,
			String str_public) {
		this.DEFAULT_PARTNER = partner;
		this.DEFAULT_SELLER = seller;
		this.PRIVATE = str_private;
		this.PUBLIC = str_public;
	}

}
