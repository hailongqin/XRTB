package com.xrtb.tools;

import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;

import org.redisson.Redisson;
import org.redisson.RedissonClient;
import org.redisson.core.MessageListener;
import org.redisson.core.RTopic;

import test.java.Config;

import com.xrtb.bidder.Controller;
import com.xrtb.commands.AddCampaign;
import com.xrtb.commands.BasicCommand;
import com.xrtb.commands.DeleteCampaign;
import com.xrtb.common.Configuration;

public class ReloadBidder {
	static BasicCommand rcv = null;
	static CountDownLatch latch;
	
	public static void main(String args[] ) throws Exception  {
		
		org.redisson.Config cfg = new org.redisson.Config();
		cfg.useSingleServer()
    	.setAddress("localhost:6379")
    	.setConnectionPoolSize(10);

		
		RedissonClient redisson = Redisson.create(cfg);
		RTopic commands = redisson.getTopic(Controller.COMMANDS);
		RTopic channel = redisson.getTopic(Controller.RESPONSES);
		latch =  new CountDownLatch(1);
		
		channel.addListener(new MessageListener<BasicCommand>() {
			@Override
			public void onMessage(String channel, BasicCommand cmd) {
				System.out.println("<<<<<<<<<<<<<<<<<" + cmd);
				rcv = cmd;
				latch.countDown();
			}
		}); 

		AddCampaign c = new AddCampaign("*","ben","ben:payday");
		DeleteCampaign d = new DeleteCampaign("*","ben", "*");
		
		commands.publish(d);
		latch.await();
		
		latch =  new CountDownLatch(1);
		commands.publish(c);
		latch.await();
		
		latch =  new CountDownLatch(1);
		commands.publish(c);
		latch.await(); 
		
		System.exit(0);
	}
}