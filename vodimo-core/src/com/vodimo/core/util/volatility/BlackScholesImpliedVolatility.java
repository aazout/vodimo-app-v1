package com.vodimo.core.util.volatility;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vodimo.core.model.Option;
import com.vodimo.core.util.VodimoException;

/*
 * Uses the Bisection method
 */
public class BlackScholesImpliedVolatility implements ImpliedVolatilityHelper {

	private static Logger logger = LogManager.getLogger(BlackScholesImpliedVolatility.class.getName());	
	
	private static double ERROR_TOLERANCE_BM = 0.001D;
	private static double ERROR_TOLERANCE_NM = 0.001D;
	private static int ITERATION_LIMIT_BM = 1000;
	private static int ITERATION_LIMIT_NM = 64;
	private static double ERROR_TOLERANCE_PARITY = 0.05D;
	
	private static long DAY_MILLIS = 24 * 60 * 60 * 1000;
	private static int ACT360_DAYS = 365;
	
	private Option call;
	private Option put;
	private double riskFreeRateValue;
	
	private NormalDistribution normCDF = new NormalDistribution(); 
	
	private double tenor;
	
	public BlackScholesImpliedVolatility(Option call, Option put, double riskFreeRate) {
		this.call = call;
		this.put = put;
		this.riskFreeRateValue = riskFreeRate;
		// Call and Put should have same quote date, strike, and expiration
		/*System.out.println("Number of days = " + (((this.call.getExpirationDate().getTime() 
				- this.call.getQuoteDate().getTime()) / DAY_MILLIS)));*/
		int numOfDays = (int) ((this.call.getExpirationDate().getTime() - this.call.getQuoteDate().getTime()) / DAY_MILLIS);		
		//numOfDays = 100;
		this.tenor = (double) numOfDays / ACT360_DAYS;		
		/*System.out.println("Tenor = " + this.tenor + " tenor x rfr = " + this.riskFreeRateValue * this.tenor);*/
	}
	
	@Override
	public double getImpliedVolatility() throws VodimoException {
		//double callIVol = newtonsMethodIV(this.call);
		double callIVol = bisectionMethodIV(this.call);
		logger.debug("callIVol {}", callIVol);
		//double putIVol = newtonsMethodIV(this.put);
		double putIVol = bisectionMethodIV(this.put);
		logger.debug("putIVol {}", putIVol);
		if(Math.abs(callIVol - putIVol) > ERROR_TOLERANCE_PARITY) 
			throw new VodimoException("No put-call parity has been violated by implied volatility calculation.");
		return (callIVol + putIVol) / 2;
	}
	
	// This method is faster
	public double newtonsMethodIV(Option o) throws VodimoException {
		double vol = 0.25D;
		int index = 1;
		
		double cpDiff = ERROR_TOLERANCE_NM + 1.0D;
		double forwardPrice = getForwardPrice(this.call, this.put);		
		
		while(Math.abs(cpDiff) > ERROR_TOLERANCE_NM) {
			//Terminate the algorithm if the iteration limit has been reached
			if(index > ITERATION_LIMIT_NM) {
				vol = -1;
				break;
			} else {
				index++;
			}
			
			double optionPrice = getOptionPrice(
					o.getOptionType(),
					o.getUnderlyingLastPrice(),
					forwardPrice,
					//o.getStrike(),
					vol,
					this.tenor
					);	
			
			cpDiff = optionPrice - o.getLast();
			
			if(Math.abs(cpDiff) > ERROR_TOLERANCE_NM) {
				double slope = vega(
						this.call.getOptionType(),
						this.call.getUnderlyingLastPrice(),
						//forwardPrice,
						this.call.getStrike(),
						vol,
						this.tenor
						);
				double yInt = cpDiff - slope*vol;
				return -yInt/slope;						
			}
			
		}
		
		return 0;
	}
	
	public double bisectionMethodIV(Option o) throws VodimoException {
		double vol = -1;		
		int index = 1;
		double cpDiff = 1.0D;
		
		double uBound = 2.0D;
		double lBound = 0.0D;
		
		double forwardPrice = getForwardPrice(this.call, this.put);			
		logger.debug("forwardPrice {}", forwardPrice);
		//System.out.println("forwardPrice = " + forwardPrice);		
		
		while(Math.abs(cpDiff) > ERROR_TOLERANCE_BM) {
			if(index > ITERATION_LIMIT_BM) {
				break;
			} else {
				index++;
			}
			
			// Get the forward price. This ensures put-call parity 
			// Calculate BS call price (and put) for current implied vol guess
			vol = (uBound + lBound) / 2.0D;			
			
			double optionPrice = getOptionPrice(
					o.getOptionType(),
					//o.getUnderlyingLastPrice(),
					forwardPrice,
					o.getStrike(),
					vol,
					this.tenor
					);	
			
			// Get difference between calculate call price and actual call price
			cpDiff = optionPrice - o.getLast();
			
			// If the calculated call price minus the actual call price is negative, 
			// adjust the lower bound to the last guest for implied volatility
			// if the calculated price minus the actual call price is positive,
			// adjust the upperbound to the last guess for implied volatility 
			if(cpDiff < 0.0D) {
				lBound = vol;
			} else if(cpDiff > 0.0D) {
				uBound = vol;
			}
			
			// If the calculated call price minus the actual call price is 0, 
			// a root has been found. Unless a root has been found, repeat 
			// the process using the new upper and lower bound			
		}
		return vol;
	}	
	
	// maturity expressed in years 6 months = 0.5
	private double getOptionPrice(String type, double underlyingPrice, double strike, double vol, double maturity) throws VodimoException {
		// FIXME: Needs to be implemented
		double price = -1;

		if(type.equalsIgnoreCase(Option.OPTION_CALL)) {			
						
			price = (underlyingPrice * normCDF.cumulativeProbability(d1(type, underlyingPrice, strike, vol, maturity))) 
					- (strike * Math.exp(-this.riskFreeRateValue * maturity) 
					* normCDF.cumulativeProbability(d2(type, underlyingPrice, strike, vol, maturity)));
			
		} else if(type.equalsIgnoreCase(Option.OPTION_PUT)) { 			
			price = normCDF.cumulativeProbability(-d2(type, underlyingPrice, strike, vol, maturity)) * (strike * Math.exp(-this.riskFreeRateValue * maturity)) 
					- (normCDF.cumulativeProbability(-d1(type, underlyingPrice, strike, vol, maturity)) * underlyingPrice); 
			
		} else {
			throw new VodimoException("Option type needs to be defined");
		}		
		return price;
	}	
	
	private double d1(String type, double underlyingPrice, double strike, double vol, double maturity) {
		return ((Math.log(underlyingPrice/strike) + ((this.riskFreeRateValue + (Math.pow(vol,2)/2)) * maturity))) / (vol * Math.sqrt(maturity)); 
	}
	
	private double d2(String type, double underlyingPrice, double strike, double vol, double maturity) {
		//return d1(type, underlyingPrice, strike, vol, maturity) - (vol * Math.sqrt(maturity));
		return ((Math.log(underlyingPrice/strike) + ((this.riskFreeRateValue - (Math.pow(vol,2)/2)) * maturity))) / (vol * Math.sqrt(maturity));
	}
	
	private double vega(String type, double underlyingPrice, double strike, double vol, double maturity) {
		return underlyingPrice * normCDF.density(d1(type, underlyingPrice, strike, vol, maturity)) * Math.sqrt(maturity);
	}	
	
	// F0 = S0 * exp(rT)
	// Discounting -> DF(T) = exp(-rT)
	// from put call parity we have F = K + exp(rt)*(c-p)
	private double getForwardPrice(Option call, Option put) {
		// Should be the same for both put and call
		//double discountRate = getDiscountRate(call.getQuoteDate(), call.getExpirationDate());				
		//return call.getStrike() + ((1/discountRate) * forwardRightTerm);
		return call.getStrike() + (Math.exp(this.riskFreeRateValue * this.tenor)) * (call.getLast() - put.getLast());
	}
	
}
