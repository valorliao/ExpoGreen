package com.expo.blackman;

import java.io.IOException;

import com.expo.coins.Coin;
import com.expo.coins.ExpoOrderType;
import com.expo.platform.ICryptoPlatform;
import com.expo.platform.QuadrigaCX;

public class BlackMan {

	public static void main(String[] args) {
		// ICryptoPlatform binance = Binance.getInstance();
		// ObjectNode result = binance.getCurrentBidAndAsk("ICXETH");
		// while (result.fieldNames().hasNext()) {
		// String s = result.fieldNames().next();
		// s = "";
		// }
		// ObjectNode result;
		// try {
		// result = binance.getCurrentBidAndAsk("ICXETH");
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// result = null;

		ICryptoPlatform quadriga = QuadrigaCX.getInstance();
		try {
			quadriga.placeOrder(Coin.BTC, Coin.CAD, ExpoOrderType.LIMITSELL,
					"0.001", "10000000");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
