package com.expo.platform;

import java.io.IOException;
import java.util.List;

import com.expo.coins.Coin;
import com.expo.coins.CoinPair;
import com.expo.coins.ExpoOrderType;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface ICryptoPlatform {

//	float transactionFee;
	public void getTransactionFee();

	// public ObjectNode getCurrentBidAndAsk(String coinPair) throws
	// IOException;

	public ObjectNode getCurrentBidAndAsk(CoinPair coinPair) throws IOException;

	public ObjectNode getCurrentBidAndAsk(Coin targetCoin, Coin baseCoin)
			throws IOException;

	// public ObjectNode placeOrder(String coinPair, ExpoOrderType orderType,
	// String quantity, String price) throws IOException, Exception;

	public ObjectNode placeOrder(CoinPair coinPair, ExpoOrderType orderType,
			String quantity, String price) throws Exception;

	public ObjectNode placeOrder(Coin targetCoin, Coin baseCoin,
			ExpoOrderType orderType, String quantity, String price)
			throws Exception;

	// public void cancelOrder(String coinPair, String orderID) throws
	// Exception;

	public void cancelOrder(CoinPair coinPair, String orderID) throws Exception;

	public void cancelOrder(Coin targetCoin, Coin baseCoin, String orderID)
			throws Exception;

	// public ObjectNode getOrderStatus(String coinPair, String orderID)
	// throws IOException, Exception;

	public ObjectNode getOrderStatus(CoinPair coinPair, String orderID);

	public ObjectNode getOrderStatus(Coin targetCoin, Coin baseCoin,
			String orderID);

	public void testConnectivity();

	public long getServerTime();

	public List<Coin> getSupportedBaseCoin();

	public List<Coin> getSupportedTargetCoin();

}
