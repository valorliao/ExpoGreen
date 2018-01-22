package com.expo.platform;

import java.util.ArrayList;
import java.util.List;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderType;
import com.binance.api.client.domain.TimeInForce;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.account.request.CancelOrderRequest;
import com.binance.api.client.domain.account.request.OrderStatusRequest;
import com.binance.api.client.domain.market.OrderBook;
import com.binance.api.client.domain.market.OrderBookEntry;
import com.binance.api.client.domain.market.TickerStatistics;
import com.expo.coins.Coin;
import com.expo.coins.CoinPair;
import com.expo.coins.ExpoOrderType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Binance implements ICryptoPlatform {

	BinanceApiRestClient client = null;
	private static ICryptoPlatform instance = null;
	ObjectMapper objectMapper = new ObjectMapper();

	private Binance() {
		BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(
				"", "");
		client = factory.newRestClient();
	}

	public static ICryptoPlatform getInstance() {
		if (instance == null) {
			instance = new Binance();
		}

		return instance;
	}


	public void getTransactionFee() {
		// TODO Auto-generated method stub

	}

	public ObjectNode getCurrentBidAndAsk(CoinPair coinPair) {
		StringBuilder sb = new StringBuilder();
		sb.append(coinPair.getTargetCoin().getSymbol()).append(
				coinPair.getBaseCoin().getSymbol());
		String coins = sb.toString();
		
		return getCurrentBidAndAsk(coins);
	}

	@SuppressWarnings("deprecation")
	public ObjectNode getCurrentBidAndAsk(String coinPair) {
		OrderBook orderBook = client.getOrderBook(coinPair, 10);

		List<OrderBookEntry> asks = orderBook.getAsks();
		List<OrderBookEntry> bids = orderBook.getBids();

		ObjectNode root = objectMapper.createObjectNode();

		ObjectNode coinPairNode = root.putObject(coinPair);

		// start time
		long serverTime = client.getServerTime();
		coinPairNode.put("startTime", serverTime);

		// current price
		TickerStatistics tickerStatistics = client
				.get24HrPriceStatistics(coinPair);
		coinPairNode.put("lastPrice", tickerStatistics.getLastPrice());

		// TODO aggregated prices
		// List<AggTrade> aggTrades = client.getAggTrades("NEOETH");

		// asks
		ArrayNode asksNode = objectMapper.createArrayNode();
		ObjectNode askNode;
		for (OrderBookEntry askBookEntry : asks) {
			askNode = objectMapper.createObjectNode();
			askNode.put(askBookEntry.getPrice(), askBookEntry.getQty());
			asksNode.add(askNode);
		}
		coinPairNode.put("ask", asksNode);

		// bids
		ArrayNode bidsNode = objectMapper.createArrayNode();
		ObjectNode bidNode;
		for (OrderBookEntry bidBookEntry : bids) {
			bidNode = objectMapper.createObjectNode();
			bidNode.put(bidBookEntry.getPrice(), bidBookEntry.getQty());
			bidsNode.add(bidNode);
		}
		coinPairNode.put("bid", bidsNode);

		// end time
		serverTime = client.getServerTime();
		coinPairNode.put("endTime", serverTime);

		return root;
	}

	public ObjectNode getCurrentBidAndAsk(Coin targetCoin, Coin baseCoin) {
		StringBuilder sb = new StringBuilder();
		sb.append(targetCoin.getSymbol()).append(baseCoin.getSymbol());
		String coins = sb.toString();

		return getCurrentBidAndAsk(coins);
	}

	public void testConnectivity() {
		client.ping();
	}

	public long getServerTime() {
		return client.getServerTime();
	}

	public List<Coin> getSupportedBaseCoin() {
		List<Coin> coins = new ArrayList<Coin>();

		coins.add(Coin.BTC);
		coins.add(Coin.ETH);
		return coins;
	}

	public List<Coin> getSupportedTargetCoin() {
		List<Coin> coins = new ArrayList<Coin>();

		coins.add(Coin.NEO);
		coins.add(Coin.ICX);
		return coins;
	}

	// TODO stop limit order
	public ObjectNode placeOrder(String coinPair, ExpoOrderType orderType,
			String quantity, String price) {
		NewOrderResponse newOrderResponse;
		NewOrder order = null;
		switch (orderType) {
		case MARKETBUY:
			order = new NewOrder(coinPair, OrderSide.BUY, OrderType.MARKET,
					TimeInForce.GTC, quantity, price);
			break;
		case MARKETSELL:
			order = new NewOrder(coinPair, OrderSide.SELL, OrderType.MARKET,
					TimeInForce.GTC, quantity, price);
			break;
		case LIMITBUY:
			order = new NewOrder(coinPair, OrderSide.BUY, OrderType.LIMIT,
					TimeInForce.GTC, quantity, price);
			break;
		case LIMITSELL:
			order = new NewOrder(coinPair, OrderSide.SELL, OrderType.LIMIT,
					TimeInForce.GTC, quantity, price);
			break;
		default:
			break;
		}
		newOrderResponse = client.newOrder(order);

		ObjectNode root = objectMapper.createObjectNode();

		root.put("coinPair", coinPair);
		root.put("orderID", newOrderResponse.getClientOrderId());

		return root;
	}

	public ObjectNode placeOrder(CoinPair coinPair, ExpoOrderType orderType,
			String quantity, String price) {
		StringBuilder sb = new StringBuilder();
		sb.append(coinPair.getTargetCoin().getSymbol()).append(
				coinPair.getBaseCoin().getSymbol());
		String coins = sb.toString();

		return placeOrder(coins, orderType, quantity, price);
	}

	public ObjectNode placeOrder(Coin targetCoin, Coin baseCoin,
			ExpoOrderType orderType, String quantity, String price) {
		StringBuilder sb = new StringBuilder();
		sb.append(targetCoin.getSymbol()).append(baseCoin.getSymbol());
		String coins = sb.toString();

		return placeOrder(coins, orderType, quantity, price);
	}

	public void cancelOrder(String coinPair, String orderID) {
		client.cancelOrder(new CancelOrderRequest(coinPair, orderID));
	}

	public void cancelOrder(CoinPair coinPair, String orderID) {
		StringBuilder sb = new StringBuilder();
		sb.append(coinPair.getTargetCoin().getSymbol()).append(
				coinPair.getBaseCoin().getSymbol());
		String coins = sb.toString();

		cancelOrder(coins, orderID);
	}

	public void cancelOrder(Coin targetCoin, Coin baseCoin, String orderID) {
		StringBuilder sb = new StringBuilder();
		sb.append(targetCoin.getSymbol()).append(baseCoin.getSymbol());
		String coins = sb.toString();

		cancelOrder(coins, orderID);
	}

	public ObjectNode getOrderStatus(String coinPair, String orderID) {
		Order order = client.getOrderStatus(new OrderStatusRequest(coinPair,
				orderID));
		ObjectNode orderNode = convertOrderStatus(order);
		ObjectNode root = objectMapper.createObjectNode();
		root.put(orderID, orderNode);

		return root;
	}

	public ObjectNode getOrdersStatus(ArrayList<String[]> orders) {
		ObjectNode root = objectMapper.createObjectNode();

		for (String[] orderPair : orders) {
			if (orderPair[0].isEmpty() || orderPair[1].isEmpty()) {
				return null;
			}
			Order order = client.getOrderStatus(new OrderStatusRequest(
					orderPair[0], orderPair[1]));
			ObjectNode orderNode = convertOrderStatus(order);
			root.put(orderPair[1], orderNode);
		}
		return root;
	}

	public ObjectNode getOrderStatus(CoinPair coinPair, String orderID) {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectNode getOrderStatus(Coin targetCoin, Coin baseCoin,
			String orderID) {
		// TODO Auto-generated method stub
		return null;
	}

	private ObjectNode convertOrderStatus(Order order) {
		ObjectNode orderNode = objectMapper.createObjectNode();

		orderNode.put("coinPair", order.getSymbol());
		orderNode.put("originalQuantity", order.getOrigQty());
		orderNode.put("executedQuantity", order.getExecutedQty());
		orderNode.put("status", order.getStatus().toString());
		orderNode.put("side", order.getSide().toString());
		orderNode.put("orderType", order.getType().toString());
		orderNode.put("price", order.getPrice());
		orderNode.put("orderId", order.getOrderId());
		// if (order.getType() == OrderType.)
		// orderNode.put("stopPrice", order.getOrderId());
		orderNode.put("timeInForce", order.getTimeInForce().toString());
		orderNode.put("time", order.getTime());

		return orderNode;
	}

}
