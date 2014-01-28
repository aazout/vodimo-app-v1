package com.vodimo.core.model;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;

import org.apache.hadoop.io.Writable;
import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Id;

@Embedded("Option")
public class Option implements Writable {
	
	public static String OPTION_CALL = "call";
	public static String OPTION_PUT = "put";
	
    @Id
    private ObjectId id;	
	
	/*
	 * Identifier for underlying security
	 */
	public String entityId;
	
	/*
	 * Last traded price of underlying
	 */
	public Double underlyingLastPrice;
	
	/*
	 * Exchange on which option is traded
	 */
	public String exchange;

	/*
	 * Option Symbol
	 */
	public String optionSymbol;	
	
	/*
	 * Call/Put
	 */
	public String optionType;
	
	/*
	 * The expiration date of the option
	 */
	public Date expirationDate;
	
	/*
	 * The date of the quote
	 */
	public Date quoteDate;
	
	/*
	 * The strike price
	 */
	public Double strike; 
	
	/*
	 * The last traded price of the option.
	 */
	public Double last;
	
	/*
	 * The bid price of the option
	 */
	public Double bid;
	
	/*
	 * The ask price of the option
	 */
	public Double ask;
	
	/*
	 * The number of contracts traded
	 */
	public Double volume;
	
	/*
	 * The open interest
	 */
	public Double openInterest;
	
	/*
	 * The implied volatility
	 */
	public Double impliedVolatility;
	
	/*
	 * The option's delta
	 */
	public Double delta;
	
	/*
	 * The option's gamma
	 */
	public Double gamma;
	
	/*
	 * The option's theta
	 */
	public Double theta;
	
	/*
	 * The option's vega
	 */
	public Double vega;

	public String getUnderlying() {
		return entityId;
	}

	public void setUnderlying(String underlying) {
		this.entityId = underlying;
	}

	public double getUnderlyingLastPrice() {
		return underlyingLastPrice;
	}

	public void setUnderlyingLastPrice(double underlyingLastPrice) {
		this.underlyingLastPrice = underlyingLastPrice;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public String getOptionType() {
		return optionType;
	}

	public void setOptionType(String optionType) {
		this.optionType = optionType;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public Date getQuoteDate() {
		return quoteDate;
	}

	public void setQuoteDate(Date quoteDate) {
		this.quoteDate = quoteDate;
	}

	public double getStrike() {
		return strike;
	}

	public void setStrike(double strike) {
		this.strike = strike;
	}

	public double getLast() {
		return last;
	}

	public void setLast(double last) {
		this.last = last;
	}

	public double getBid() {
		return bid;
	}

	public void setBid(double bid) {
		this.bid = bid;
	}

	public double getAsk() {
		return ask;
	}

	public void setAsk(double ask) {
		this.ask = ask;
	}

	public Double getVolume() {
		return volume;
	}

	public void setVolume(Double volume) {
		this.volume = volume;
	}

	public double getOpenInterest() {
		return openInterest;
	}

	public void setOpenInterest(double openInterest) {
		this.openInterest = openInterest;
	}

	public double getImpliedVolatility() {
		return impliedVolatility;
	}

	public void setImpliedVolatility(double impliedVolatility) {
		this.impliedVolatility = impliedVolatility;
	}

	public double getDelta() {
		return delta;
	}

	public void setDelta(double delta) {
		this.delta = delta;
	}

	public double getGamma() {
		return gamma;
	}

	public void setGamma(double gamma) {
		this.gamma = gamma;
	}

	public double getTheta() {
		return theta;
	}

	public void setTheta(double theta) {
		this.theta = theta;
	}

	public double getVega() {
		return vega;
	}

	public void setVega(double vega) {
		this.vega = vega;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getOptionSymbol() {
		return optionSymbol;
	}

	public void setOptionSymbol(String optionSymbol) {
		this.optionSymbol = optionSymbol;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.hadoop.io.Writable#readFields(java.io.DataInput)
	 */
	@Override
	public void readFields(DataInput arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void write(DataOutput arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}	
		
}
