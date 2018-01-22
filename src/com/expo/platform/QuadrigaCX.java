package com.expo.platform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.OkHttpClient;

import org.apache.commons.codec.binary.Hex;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

//import org.json.JSONObject;
import com.expo.coins.Coin;
import com.expo.coins.CoinPair;
import com.expo.coins.ExpoOrderType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.internal.LinkedTreeMap;

public class QuadrigaCX implements ICryptoPlatform {

	private final String API_URL = "https://api.quadrigacx.com/v2/";
	private final String API_KEY = "";//
	private final String API_SECRET = "";//
	private final String API_CLIENT = "";//
	
	QuadrigaAPI APIClient = null;
	private static ICryptoPlatform instance = null;
	
	ObjectMapper objectMapper = new ObjectMapper();

	private QuadrigaCX() {
		OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
		// builder.readTimeout(10, TimeUnit.SECONDS);
		// builder.connectTimeout(5, TimeUnit.SECONDS);
		//
		// builder.addInterceptor(new Interceptor() {
		// @Override
		// public Response intercept(Chain chain) throws IOException {
		// Request request = chain.request().newBuilder().addHeader("key",
		// "value").build();
		// return chain.proceed(request);
		// }
		// });
		//
		// builder.addInterceptor(new UnauthorisedInterceptor(context));
		OkHttpClient client = builder.build();

		Retrofit retrofit = new Retrofit.Builder().baseUrl(API_URL)
				.addConverterFactory(GsonConverterFactory.create())
				.client(client).build();
		APIClient = retrofit.create(QuadrigaAPI.class);
	}
	
	public static ICryptoPlatform getInstance() {
		if (instance == null) {
			instance = new QuadrigaCX();
		}

		return instance;
	}
	
	public void getTransactionFee() {
		// TODO Auto-generated method stub

	}

	public ObjectNode getCurrentBidAndAsk(String coinPair) throws IOException {
		ObjectNode root = objectMapper.createObjectNode();
		if (coinPair.isEmpty()) {
			coinPair = this.getCoinPair(Coin.BTC, Coin.CAD);
		}
		LinkedTreeMap<String, Object> trades = APIClient
				.getCurrentTrades(coinPair).execute().body();

		ObjectNode coinPairNode = root.putObject(coinPair);
		
		// start time
		String serverTime = trades.get("timestamp").toString();
		coinPairNode.put("startTime", serverTime);

		// current price
		String lastPrice = trades.get("last").toString();
		coinPairNode.put("lastPrice", lastPrice);

		LinkedTreeMap<String, Object> currentStatus = APIClient
				.getCurrentBidAndAsk(coinPair).execute().body();

		// asks
		ArrayList<ArrayList<String>> asksBookEntry = (ArrayList<ArrayList<String>>) currentStatus
				.get("asks");
		ArrayNode asksNode = objectMapper.createArrayNode();
		ObjectNode askNode;
		for (ArrayList<String> askBookEntry : asksBookEntry) {
			askNode = objectMapper.createObjectNode();
			askNode.put(askBookEntry.get(0), askBookEntry.get(1));
			asksNode.add(askNode);
		}

		coinPairNode.put("ask", asksNode);

		// bids
		ArrayList<ArrayList<String>> bidsBookEntry = (ArrayList<ArrayList<String>>) currentStatus
				.get("bids");
		ArrayNode bidsNode = objectMapper.createArrayNode();
		ObjectNode bidNode;
		for (ArrayList<String> bidBookEntry : bidsBookEntry) {
			bidNode = objectMapper.createObjectNode();
			bidNode.put(bidBookEntry.get(0), bidBookEntry.get(1));
			bidsNode.add(bidNode);
		}

		coinPairNode.put("bid", bidsNode);

		// endtime
		serverTime = currentStatus.get("timestamp").toString();
		coinPairNode.put("startTime", serverTime);

		return root;
	}

	public ObjectNode getCurrentBidAndAsk(CoinPair coinPair) throws IOException {
		return getCurrentBidAndAsk(this.getCoinPair(coinPair.getTargetCoin(),
				coinPair.getBaseCoin()));
	}

	public ObjectNode getCurrentBidAndAsk(Coin targetCoin, Coin baseCoin)
			throws IOException {
		return getCurrentBidAndAsk(this.getCoinPair(targetCoin, baseCoin));
	}

	public ObjectNode placeOrder(String coinPair, ExpoOrderType orderType,
			String quantity, String price) throws Exception {
		if (coinPair.isEmpty()) {
			coinPair = this.getCoinPair(Coin.BTC, Coin.CAD);
		}

		long nonce = System.currentTimeMillis() / 1000L;
		StringBuilder sb = new StringBuilder();
		sb.append(nonce).append(API_CLIENT).append(API_KEY);
		String signature = this.encode(API_SECRET, sb.toString());
		
		LinkedTreeMap<String, Object> order = null;
		ObjectNode root = objectMapper.createObjectNode();

		switch (orderType) {
		case MARKETBUY:
			order = APIClient.placeMarketBuyOrder(API_KEY, signature, nonce, quantity,
							coinPair).execute().body();
			break;
		case MARKETSELL:
			order = APIClient.placeMarketSellOrder(API_KEY, signature, nonce, quantity,
							coinPair).execute().body();
			// root.put("orders_matched",
			// order.get("orders_matched").toString());

			break;
		case LIMITBUY:
			order = APIClient.placeLimitBuyOrder(API_KEY, signature, nonce, quantity,
							price, coinPair).execute().body();
			root.put("orderID", order.get("id").toString());
			root.put("price", order.get("price").toString());
			root.put("datetime", order.get("datetime").toString());
			
			break;
		case LIMITSELL:
			order = APIClient.placeLimitSellOrder(API_KEY, signature, nonce, quantity,
							price, coinPair).execute().body();
			root.put("orderID", order.get("id").toString());
			root.put("price", order.get("price").toString());
			root.put("datetime", order.get("datetime").toString());

			break;
		default:
			break;
		}

		root.put("coinPair", coinPair);

		return root;
	}

	public ObjectNode placeOrder(CoinPair coinPair, ExpoOrderType orderType,
			String quantity, String price) throws Exception {
		ObjectNode result = placeOrder(
				this.getCoinPair(coinPair.getTargetCoin(),
						coinPair.getBaseCoin()),
				orderType, quantity, price);
		result.put("targetCoin", coinPair.getTargetCoin().getSymbol());
		result.put("baseCoin", coinPair.getBaseCoin().getSymbol());
		return result;
	}

	public ObjectNode placeOrder(Coin targetCoin, Coin baseCoin,
			ExpoOrderType orderType, String quantity, String price)
			throws Exception {
		ObjectNode result = placeOrder(this.getCoinPair(targetCoin, baseCoin),
				orderType, quantity, price);
		result.put("targetCoin", targetCoin.getSymbol());
		result.put("baseCoin", baseCoin.getSymbol());
		return result;
	}

	public void cancelOrder(String coinPair, String orderID) throws Exception {
		long nonce = System.currentTimeMillis() / 1000L;
		StringBuilder sb = new StringBuilder();
		sb.append(nonce).append(API_CLIENT).append(API_KEY);
		String signature = this.encode(API_SECRET, sb.toString());

		boolean result = APIClient.cancelOrder(API_KEY, signature, nonce,
				orderID).execute().body();
		result = true;
	}

	public void cancelOrder(CoinPair coinPair, String orderID) throws Exception {
		cancelOrder(
				getCoinPair(coinPair.getTargetCoin(), coinPair.getBaseCoin()),
				orderID);

	}

	public void cancelOrder(Coin targetCoin, Coin baseCoin, String orderID)
			throws Exception {
		cancelOrder(getCoinPair(targetCoin, baseCoin), orderID);

	}

	public ObjectNode getOrderStatus(String coinPair, String orderID)
			throws Exception {
		long nonce = System.currentTimeMillis() / 1000L;
		StringBuilder sb = new StringBuilder();
		sb.append(nonce).append(API_CLIENT).append(API_KEY);
		String signature = this.encode(API_SECRET, sb.toString());

		ObjectNode root = objectMapper.createObjectNode();

		LinkedTreeMap<String, Object> result = APIClient
				.getOrderStatus(API_KEY, signature, nonce, orderID).execute()
				.body();

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

	public void testConnectivity() {
		// TODO Auto-generated method stub

	}

	@Deprecated
	public long getServerTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<Coin> getSupportedBaseCoin() {
		List<Coin> coins = new ArrayList<Coin>();

		coins.add(Coin.CAD);
		coins.add(Coin.USD);
		return coins;
	}

	public List<Coin> getSupportedTargetCoin() {
		List<Coin> coins = new ArrayList<Coin>();

		coins.add(Coin.BTC);
		coins.add(Coin.ETH);
		return coins;
	}

	public interface QuadrigaAPI {
		@GET("ticker")
		Call<LinkedTreeMap<String, Object>> getCurrentTrades(
				@Query("book") String coinPair);

		@GET("order_book")
		Call<LinkedTreeMap<String, Object>> getCurrentBidAndAsk(
				@Query("book") String coinPair);

		@POST("buy")
		Call<LinkedTreeMap<String, Object>> placeLimitBuyOrder(
				@Query("key") String key, @Query("signature") String signature,
				@Query("nonce") long nonce, @Query("amount") String amount,
				@Query("price") String price, @Query("book") String coinPair);

		@POST("buy")
		Call<LinkedTreeMap<String, Object>> placeMarketBuyOrder(
				@Query("key") String key, @Query("signature") String signature,
				@Query("nonce") long nonce, @Query("amount") String amount,
				@Query("book") String coinPair);

		@POST("sell")
		Call<LinkedTreeMap<String, Object>> placeLimitSellOrder(
				@Query("key") String key, @Query("signature") String signature,
				@Query("nonce") long nonce, @Query("amount") String amount,
				@Query("price") String price, @Query("book") String coinPair);

		@POST("sell")
		Call<LinkedTreeMap<String, Object>> placeMarketSellOrder(
				@Query("key") String key, @Query("signature") String signature,
				@Query("nonce") long nonce, @Query("amount") String amount,
				@Query("book") String coinPair);

		@POST("cancel_order")
		Call<Boolean> cancelOrder(@Query("key") String key,
				@Query("signature") String signature,
				@Query("nonce") long nonce, @Query("id") String orderId);

		@POST("lookup_order")
		Call<LinkedTreeMap<String, Object>> getOrderStatus(
				@Query("key") String key,
				@Query("signature") String signature,
				@Query("nonce") long nonce, @Query("id") String orderId);

	}

	private String getCoinPair(Coin targetCoin, Coin baseCoin) {
		return targetCoin.getSymbol() + "_" + baseCoin.getSymbol();
	}

	private String encode(String key, String data) throws Exception {
		Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
		SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"),
				"HmacSHA256");
		sha256_HMAC.init(secret_key);

		return Hex.encodeHexString(sha256_HMAC.doFinal(data.getBytes("UTF-8")));
	}
}
