package com.expo.coins;

public enum Coin {
	BTC("BitCoin", "BTC"), ETH("Etherium", "ETH"), NEO("Neo", "NEO"), ICX(
			"Icx", "ICX"), USD("USD", "USD"), CAD("CAD", "CAD");

	private final String name;
	private final String symbol;

	Coin(String name, String symbol) {
		this.name = name;
		this.symbol = symbol;
	}

	public String getName() {
		return name;
	}

	public String getSymbol() {
		return symbol;
	}

}
