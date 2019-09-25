package com.masasdani.paypal.controller;

import javax.servlet.http.HttpServletRequest;

import com.masasdani.paypal.service.DBHelper;
import com.masasdani.paypal.service.PayPalVerifyPayment;
import com.masasdani.paypal.util.HttpClientUtil;
import com.stripe.model.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.masasdani.paypal.config.PaypalPaymentIntent;
import com.masasdani.paypal.config.PaypalPaymentMethod;
import com.masasdani.paypal.service.PaypalService;
import com.masasdani.paypal.util.URLUtils;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.net.RequestOptions;



import java.util.Date;

@Controller
@RequestMapping("/")
public class PaymentController {
	
	public static final String PAYPAL_SUCCESS_URL = "pay/success";
	public static final String PAYPAL_CANCEL_URL = "pay/cancel";
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private PaypalService paypalService;
	
	@RequestMapping(method = RequestMethod.GET)
	public String index(){
		return "index";
	}

	@RequestMapping(method = RequestMethod.POST, value = "pay")
	public String pay(HttpServletRequest request){
		String cancelUrl = URLUtils.getBaseURl(request) + "/" + PAYPAL_CANCEL_URL;
		String successUrl = URLUtils.getBaseURl(request) + "/" + PAYPAL_SUCCESS_URL;
		try {
			Payment payment = paypalService.createPayment(
					4.00,
					"USD",
					PaypalPaymentMethod.paypal,
					PaypalPaymentIntent.sale,
					"payment description",
					cancelUrl,
					successUrl);
			for(Links links : payment.getLinks()){
				if(links.getRel().equals("approval_url")){
					return "redirect:" + links.getHref();
				}
			}
		} catch (PayPalRESTException e) {
			log.error(e.getMessage());
		}
		return "redirect:/";
	}

	@RequestMapping(method = RequestMethod.GET, value = PAYPAL_CANCEL_URL)
	public String cancelPay(){
		return "cancel";
	}

	@RequestMapping(method = RequestMethod.GET, value = PAYPAL_SUCCESS_URL)
	public String successPay(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId){
		try {
			Payment payment = paypalService.executePayment(paymentId, payerId);
			if(payment.getState().equals("approved")){
				return "success";
			}
		} catch (PayPalRESTException e) {
			log.error(e.getMessage());
		}
		return "redirect:/";
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "pay/verify")
	public String payVerify(@RequestBody JSONObject jsonObject){
		System.out.println("===============payment verify===============");
		String pId = jsonObject.get("paymentId").toString();
		String phone = jsonObject.get("phone").toString();
        String sql_uid = "select *from account where phone=" + phone;
        int uId = DBHelper.select_data(sql_uid,"id");
		Double amount = Double.parseDouble(jsonObject.get("amount").toString());
		System.out.println(new Date());
        System.out.println("paymentId:"+pId+" uid:"+uId+" amount:"+amount);
        PayPalVerifyPayment paymentV = new PayPalVerifyPayment();
        boolean success = false;
        try {
            success = paymentV.verifyPayment(pId,amount);
        } catch (Exception e) {
            e.printStackTrace();
        }

		JSONObject responseJson = new JSONObject();
        if (success) {
            System.out.println("支付完成");
            String sql = "select * from finace where account_id=" + uId;
            double balance = DBHelper.select_data(sql,"balance") + amount*100;

//            String sql1 = "update finace set balance=? where account_id=" + uId;
//            DBHelper.update_balance(sql1,balance);
            final JSONObject financeJson = new JSONObject();
            try {
                financeJson.put("uid", uId);
                financeJson.put("balance", balance);
            } catch (Exception e) {
                e.printStackTrace();
            }
            JSONObject ret = HttpClientUtil.doPost("http://abj-elogic-test1.yunba.io:9002/admin_api/finace?appkey=56a0a88c4407a3cd028ac2fe",financeJson);
            String status = ret.get("status").toString();
            if (status.equals("0")) {
                System.out.println("余额更新成功!");
				responseJson.put("status",0);
            }
        } else {
            System.out.println("支付校验失败");
			responseJson.put("status",1);
        }
		return responseJson.toString();
	}
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "pay/webhook")
	public String payWebhook(@RequestBody JSONObject jsonObject){
		System.out.println("===============web hook===============");
		System.out.println(jsonObject.toString());
		return null;
	}

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "pay/stripe")
    public String payStripe(@RequestBody JSONObject jsonObject){
        System.out.println("===============Stripe Pay===============");

        String tokenId = jsonObject.get("token").toString();
        String phone = jsonObject.get("phone").toString();
        String sql_uid = "select *from account where phone=" + phone;
        int uId = DBHelper.select_data(sql_uid,"id");
        Double amount = Double.parseDouble(jsonObject.get("amount").toString());
        System.out.println(new Date());
        System.out.println("token:"+tokenId+" uid:"+uId+" amount:"+amount);

        Stripe.apiKey = "";

        Map<String, Object> chargeMap = new HashMap<String, Object>();
        chargeMap.put("amount", amount*100);
        chargeMap.put("currency", "usd");
        chargeMap.put("source", tokenId); // obtained via Stripe.js

        try {
            Charge charge = Charge.create(chargeMap);
            System.out.println("stripe charge:" + charge);
        } catch (StripeException e) {
            e.printStackTrace();
        }
        return null;
    }

}
