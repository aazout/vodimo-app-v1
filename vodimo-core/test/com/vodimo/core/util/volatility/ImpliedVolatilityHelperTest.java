package com.vodimo.core.util.volatility;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import com.vodimo.core.model.Option;
import com.vodimo.core.util.VodimoException;

public class ImpliedVolatilityHelperTest {
		
	@Test
	public void testImpliedVolatility() {		
		
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
		Date evaluationDate = new Date();
		try {
			evaluationDate = sdf.parse("01/13/12");
		} catch (ParseException e2) {
			e2.printStackTrace();
		}
								
		// Feb 08, 2013	23.00	-3.15	22.85	23.35	707	111	AAPL	520.00	Feb 08, 2013	24.05	0.70	23.80	24.30	94	93
		
		// Create call option
		Option call = new Option();
		call.setStrike(520);
		call.setOptionType("call");
		call.setUnderlyingLastPrice(520.3);
		call.setLast(22.85);	
		call.setQuoteDate(evaluationDate);
		
		
		Date maturity = new Date();
		try {
			maturity = sdf.parse("02/13/12");
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		
		call.setExpirationDate(maturity);	
				
		// Create put option with same strike/maturity		
		Option put = new Option();
		put.setStrike(520);
		put.setOptionType("put");
		put.setUnderlyingLastPrice(520.3);
		put.setLast(24.05);				
		put.setExpirationDate(maturity);
		put.setQuoteDate(evaluationDate);
		
		RiskFreeRate rfr = TreasuryBillRiskFreeRate.newInstance();
		ImpliedVolatilityHelper IVhelper = new BlackScholesImpliedVolatility(call, put, rfr.getRiskFreeRate(evaluationDate));
			
		double vol;
		try {

			vol = IVhelper.getImpliedVolatility();
			System.out.println("Implied volatility = " + vol);
			
			assertTrue(true);	
		} catch (VodimoException e) {
			e.printStackTrace();
			assertTrue(false);	
		}		
			
	}

}
