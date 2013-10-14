/*
 * This class uses QuantLib open source computational finance library to calculate
 * implied volatility
 */

package com.vodimo.core.util.volatility;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quantlib.Actual365Fixed;
import org.quantlib.BlackConstantVol;
import org.quantlib.BlackScholesProcess;
import org.quantlib.BlackVolTermStructureHandle;
import org.quantlib.Calendar;
import org.quantlib.Date;
import org.quantlib.DayCounter;
import org.quantlib.EuropeanExercise;
import org.quantlib.Exercise;
import org.quantlib.FlatForward;
import org.quantlib.GeneralizedBlackScholesProcess;
import org.quantlib.Month;
import org.quantlib.Option;
import org.quantlib.Payoff;
import org.quantlib.PlainVanillaPayoff;
import org.quantlib.QuoteHandle;
import org.quantlib.Settings;
import org.quantlib.SimpleQuote;
import org.quantlib.TARGET;
import org.quantlib.VanillaOption;
import org.quantlib.YieldTermStructureHandle;

import com.vodimo.core.util.VodimoException;

public class QuantLibImpliedVolatilityHelper {

	private static Logger logger = LogManager.getLogger(QuantLibImpliedVolatilityHelper.class.getName());	
	
	static { // Load QuantLib
		try { 
			System.loadLibrary("QuantLibJNI"); 
		} catch (RuntimeException e) { 
			e.printStackTrace(); 
		}
	}	
	
	/*
	 * Calculates the implied volatility for European option. The underlying price should be forward 
	 * price in order to maintain put-call parity. 
	 */

	private static double IMPL_VOL_TOLERANCE = 0.05;
	
	public QuantLibImpliedVolatilityHelper(java.util.Date evaluationDate) {
		Settings.instance().setEvaluationDate(getQuantLibDate(evaluationDate));
	}
	
	public double impliedVolatility(double strike, Date maturity, double underlyingPrice, double riskFreeRate, double lastQuote, String optionType, Date quoteDate) 
			throws VodimoException {
				
		Calendar calendar = new TARGET();
		
		DayCounter dayCounter  = new Actual365Fixed();
		
        Option.Type type = getOptionType(optionType);
                
        Exercise europeanExercise = new EuropeanExercise(maturity);
		
        Payoff payoff = new PlainVanillaPayoff(type, strike);
		VanillaOption option = new VanillaOption(payoff, europeanExercise);
		
		//Date now = Date.todaysDate();
		// TODO: What to add for settlement date? Typically two days?
		//Date settlementDate = now.add(new Period(0, TimeUnit.Days));
		 		
		
		
        // define the underlying asset and the yield/dividend/volatility curves
        QuoteHandle underlyingH = new QuoteHandle(new SimpleQuote(underlyingPrice));
        
        YieldTermStructureHandle flatTermStructure = 
        		new YieldTermStructureHandle(new FlatForward(quoteDate, riskFreeRate, dayCounter));                
        BlackVolTermStructureHandle flatVolatility = 
        		new BlackVolTermStructureHandle(new BlackConstantVol(quoteDate, calendar, 0.00, dayCounter));      
        GeneralizedBlackScholesProcess stochasticProcess = new BlackScholesProcess(
        		underlyingH,
        		flatTermStructure,
        		flatVolatility);	    
        
        double implVol = -1;
        try {
        	implVol = option.impliedVolatility(lastQuote, stochasticProcess);
        } catch (RuntimeException rte) {
        	logger.error("Implied volatility calc failed", rte);
        	//rte.printStackTrace();
        	throw new VodimoException(rte);
        } 
        return implVol;                        
	}
	
	/* 
	 * Calculate implied volatility given a call and put option. This method requires 
	 * that the call and put options are the same strike and maturity. 
	 */
	public double impliedVolatility(com.vodimo.core.model.Option call, com.vodimo.core.model.Option put, double riskFreeRate) 
			throws VodimoException {
		//TODO: Check to ensure that call and put are same strike and maturity
		if((call.getExpirationDate() == put.getExpirationDate()) | (call.getStrike() == put.getStrike())) {
			Date maturity = getQuantLibDate(call.getExpirationDate());
			Date quoteDate = getQuantLibDate(call.getQuoteDate()); 
			double forward = getForwardPrice(call, put, riskFreeRate, maturity, quoteDate);				
			double callIV =  impliedVolatility(
					call.getStrike(), 
					maturity, 
					//call.getUnderlyingLastPrice(), //Should be near ATM
					forward,
					riskFreeRate, 
					call.getLast(),
					call.getOptionType(),
					quoteDate);
			//System.out.println("callIV = " + callIV);
			double putIV =  impliedVolatility(
					put.getStrike(), 
					maturity, 
					//call.getUnderlyingLastPrice(), //Should be near ATM
					forward,
					riskFreeRate, 
					put.getLast(),
					put.getOptionType(),
					quoteDate);
			//System.out.println("putIV = " + putIV);
			if(Math.abs(callIV - putIV) > IMPL_VOL_TOLERANCE) {
				throw new VodimoException("Large disparity between call and put implied volatilities!");
			}
			// Return the average
			return ((callIV + putIV) / 2);
		} else {
			throw new VodimoException("Strike and maturity are not the same on call and put option!");
		}
	}
	
	/*
	 * use put call parity relationship for european options
	 * c - p = exp(-rt)*(f-K)
	 * hence f = K + exp(rt)*(c-p)
	 * f = K + D*(c-p)
	 * c + PV(x) = p + s
	 */
	private double getForwardPrice(com.vodimo.core.model.Option call, 
			com.vodimo.core.model.Option put, 
			double riskFreeRate, 
			Date maturityDate,
			Date quoteDate) {	
		        						
		double discountRate = getDiscountRate(riskFreeRate, quoteDate, maturityDate);				
		double forwardRightTerm = call.getLast() - put.getLast();
		return call.getStrike() + ((1/discountRate) * forwardRightTerm);
		
	}
	
	private Option.Type getOptionType(String optionType) {
		if(optionType.equals("call")) {
			return Option.Type.Call;
		} else { 
			return Option.Type.Put;
		}
	}
	
	private Date getQuantLibDate(java.util.Date d) {
		java.util.Calendar cal = java.util.Calendar.getInstance();
		cal.setTime(d);
		int day = cal.get(java.util.Calendar.DAY_OF_MONTH);
		int year = cal.get(java.util.Calendar.YEAR);		
		Month month = Month.swigToEnum(cal.get(java.util.Calendar.MONTH) + 1);
		return new Date(day, month, year);
	}
	
	private double getDiscountRate(double riskFreeRate, Date quoteDate, Date maturity) {		
		DayCounter dayCounter  = new Actual365Fixed();
		YieldTermStructureHandle flatTermStructure = 
        		new YieldTermStructureHandle(new FlatForward(quoteDate, riskFreeRate, dayCounter));
		return flatTermStructure.discount(maturity);
	}

}
