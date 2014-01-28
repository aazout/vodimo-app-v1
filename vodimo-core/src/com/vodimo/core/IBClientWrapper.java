package com.vodimo.core;

/*
 * This class wraps the IB Java client to extract market data for 
 * use in core algorithms. 
 */

import java.util.HashMap;
import java.util.Map;

import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.UnderComp;
import com.vodimo.core.model.HistoricalDataRequest;

public class IBClientWrapper implements EWrapper {

	private EClientSocket m_client = new EClientSocket(this);
	
	private static IBClientWrapper wrapper;
	
	private Map<String, Object> listeners = new HashMap<String, Object>();
	
	/*
	 * TWC must be running on local machine
	 */
	private String host = ""; //leave null to connect to local host
	private int port = 7496;
	private int clientId = 0;
	
	private IBClientWrapper() {}
	
	public static IBClientWrapper newInstance() {
		if(wrapper == null) {
			wrapper = new IBClientWrapper();
			return wrapper;
		} else {
			return wrapper;
		}
	}
	
	public void connect() {
		m_client.eConnect(this.host, this.port, this.clientId);		
	}
	
	public boolean isConnected() {
		return m_client.isConnected();
	}
	
	public void setListener(Object l) {
		String cname = l.getClass().getName();
		listeners.put(cname, l);
	}

	@Override
	public void connectionClosed() {
		
	}

	@Override
	public void error(Exception arg0) {
		System.out.println("error(Exception arg0)");
		arg0.printStackTrace();		
	}

	@Override
	public void error(String arg0) {
		System.out.println("error(String arg0)");
		Exception e = new Exception(arg0);
		e.printStackTrace();
	}

	@Override
	public void error(int arg0, int arg1, String arg2) {		
		
	}

	@Override
	public void accountDownloadEnd(String arg0) {
		
	}

	@Override
	public void bondContractDetails(int arg0, ContractDetails arg1) {
		
	}

	@Override
	public void contractDetails(int arg0, ContractDetails arg1) {
		
	}

	@Override
	public void contractDetailsEnd(int arg0) {
		
	}

	@Override
	public void currentTime(long arg0) {
		
	}

	@Override
	public void deltaNeutralValidation(int arg0, UnderComp arg1) {
		
	}

	@Override
	public void execDetails(int arg0, Contract arg1, Execution arg2) {
		
	}

	@Override
	public void execDetailsEnd(int arg0) {
		
	}

	@Override
	public void fundamentalData(int arg0, String arg1) {
		
	}
	
	public void requestHistoricalData(HistoricalDataRequest r) {
		//System.out.println("requestHistoricalData()");
		m_client.reqHistoricalData(r.getId(), r.getContract(), r.getEndDateTime(), 
				r.getDurationStr(), r.getBarSizeSetting(), r.getWhatToShow(), r.getUseRTH(), r.getFormatDate());
	}
	
	@Override
	public void historicalData(int arg0, String arg1, double arg2, double arg3,
			double arg4, double arg5, int arg6, int arg7, double arg8,
			boolean arg9) {		
		//System.out.println("historicalData()");
		if(listeners.get(IBClientHistoricalDataListener.class.getName()) != null) {
			IBClientHistoricalDataListener hdl = 
					(IBClientHistoricalDataListener) listeners.get(IBClientHistoricalDataListener.class.getName());
			hdl.historicalDataEvent(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
		} else {
			System.out.println("Received historical data but no listener attached.");
		}
		
	}

	@Override
	public void managedAccounts(String arg0) {
		
	}

	@Override
	public void marketDataType(int arg0, int arg1) {
		
	}

	@Override
	public void nextValidId(int arg0) {
		
	}

	@Override
	public void openOrder(int arg0, Contract arg1, Order arg2, OrderState arg3) {
		
	}

	@Override
	public void openOrderEnd() {
		
	}

	@Override
	public void orderStatus(int arg0, String arg1, int arg2, int arg3,
			double arg4, int arg5, int arg6, double arg7, int arg8, String arg9) {
		
	}

	@Override
	public void realtimeBar(int arg0, long arg1, double arg2, double arg3,
			double arg4, double arg5, long arg6, double arg7, int arg8) {
		
	}

	@Override
	public void receiveFA(int arg0, String arg1) {
		
	}

	@Override
	public void scannerData(int arg0, int arg1, ContractDetails arg2,
			String arg3, String arg4, String arg5, String arg6) {
		
	}

	@Override
	public void scannerDataEnd(int arg0) {
		
	}

	@Override
	public void scannerParameters(String arg0) {
		
	}

	@Override
	public void tickEFP(int arg0, int arg1, double arg2, String arg3,
			double arg4, int arg5, String arg6, double arg7, double arg8) {
		
	}

	@Override
	public void tickGeneric(int arg0, int arg1, double arg2) {
		
	}

	@Override
	public void tickOptionComputation(int arg0, int arg1, double arg2,
			double arg3, double arg4, double arg5, double arg6, double arg7,
			double arg8, double arg9) {
		
	}

	@Override
	public void tickPrice(int arg0, int arg1, double arg2, int arg3) {
		
	}

	@Override
	public void tickSize(int arg0, int arg1, int arg2) {
		
	}

	@Override
	public void tickSnapshotEnd(int arg0) {
		
	}

	@Override
	public void tickString(int arg0, int arg1, String arg2) {
		
	}

	@Override
	public void updateAccountTime(String arg0) {
		
	}

	@Override
	public void updateAccountValue(String arg0, String arg1, String arg2,
			String arg3) {
		
	}

	@Override
	public void updateMktDepth(int arg0, int arg1, int arg2, int arg3,
			double arg4, int arg5) {
		
	}

	@Override
	public void updateMktDepthL2(int arg0, int arg1, String arg2, int arg3,
			int arg4, double arg5, int arg6) {
		
	}

	@Override
	public void updateNewsBulletin(int arg0, int arg1, String arg2, String arg3) {
		
	}

	@Override
	public void updatePortfolio(Contract arg0, int arg1, double arg2,
			double arg3, double arg4, double arg5, double arg6, String arg7) {
		
	}

	@Override
	public void commissionReport(CommissionReport commissionReport) {
		
	}
	
}
