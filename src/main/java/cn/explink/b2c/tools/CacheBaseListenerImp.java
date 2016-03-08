package cn.explink.b2c.tools;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import cn.explink.dao.GetDmpDAO;
import cn.explink.domain.Branch;
import cn.explink.domain.Customer;
import cn.explink.domain.User;

/**
 * 对基础信息进行缓存
 * 
 * @author Administrator
 *
 */
@Service
public class CacheBaseListenerImp implements CacheBaseListener, ApplicationListener<ContextRefreshedEvent> {

	private Logger logger = LoggerFactory.getLogger(CacheBaseListenerImp.class);

	@Autowired
	GetDmpDAO getDmpdao;

	private static final String CUSTOMER_CACHE_NAME = "customerCache";
	private static final String BRANCH_CACHE_NAME = "branchCache";
	private static final String USER_CACHE_NAME = "userCache";
	
	private static final RedisMap<Long, Customer> customerCache = new RedisMapCommonImpl<Long, Customer>(CUSTOMER_CACHE_NAME);
	private static final RedisMap<Long, Branch> branchCache = new RedisMapCommonImpl<Long, Branch>(BRANCH_CACHE_NAME);
	private static final RedisMap<Long, User> userCache = new RedisMapCommonImpl<Long, User>(USER_CACHE_NAME);
	
	private void initCustomerList() {
		List<Customer> customerlist = getDmpdao.getAllCustomers();
		if(customerlist != null){
			for(Customer customer : customerlist){
				customerCache.put(customer.getCustomerid(), customer);
			}
		}
		logger.info("初始化customer成功");
	}

	private void initBranchList() {
		List<Branch> branchlist = getDmpdao.getAllBranchs();
		if(branchlist != null){
			for(Branch branch : branchlist){
				branchCache.put(branch.getBranchid(), branch);
			}
		}
		logger.info("初始化branch成功");
	}

	private void initUserList() {
		List<User> userlist = getDmpdao.getAllUsers();
		if(userlist != null){
			for(User user : userlist){
				userCache.put(user.getUserid(), user);
			}
		}
		logger.info("初始化user成功");
	}

	@Override
	public void initAll() {
		this.initCustomerList();
		this.initBranchList();
		this.initUserList();
		logger.info("初始化oms基础数据成功");
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
//		this.initAll();
	}

	@Override
	public void onChange(Map<String, String> parameters) {
		if (parameters == null) {
			this.initAll();
		}
		logger.info("oms基础信息同步成功,key={}",parameters.keySet());
		
		if (parameters.keySet().contains("customer")) {
			this.initCustomerList();
		}
		if (parameters.keySet().contains("branch")) {
			this.initBranchList();
		}
		if (parameters.keySet().contains("user")) {
			this.initUserList();
		}
	}

	@Override
	public Customer getCustomer(long customerid) {
		try {
			logger.info("查找customer，id=" + String.valueOf(customerid));
			Customer customer = customerCache.get(customerid);
			if(customer == null){
				logger.info("没有找到对应的customer缓存，id=" + String.valueOf(customerid));
				customer = getDmpdao.getCustomer(customerid);
				if(customer != null) {
					customerCache.put(customer.getCustomerid(), customer);
					logger.info("重新读取customer并加入缓存，id=" + String.valueOf(customerid));
				}
			}
			return customer;
			
		} catch (Exception e) {
			logger.error("获取供货商异常", e);
		}
		return null;
	}

	@Override
	public Branch getBranch(long branchid) {
		try {
			logger.info("查找branch，id=" + String.valueOf(branchid));
			Branch branch = branchCache.get(branchid);
			if(branch == null){
				logger.info("没有找到对应的branch缓存，id=" + String.valueOf(branchid));
				branch = getDmpdao.getNowBranch(branchid);
				if(branch != null) {
					branchCache.put(branch.getBranchid(), branch);
					logger.info("重新读取branch并加入缓存，id=" + String.valueOf(branchid));
				}
			}
			return branch;

		} catch (Exception e) {
			logger.error("获取站点信息异常", e);
		}
		return null;
	}

	@Override
	public User getUser(long userid) {
		try {
			logger.info("查找user，id=" + String.valueOf(userid));
			User user = userCache.get(userid);
			if(user == null){
				logger.info("没有找到对应的user缓存，id=" + String.valueOf(userid));
				user = getDmpdao.getUserById(userid);
				if(user != null) {
					userCache.put(user.getUserid(), user);
					logger.info("重新读取user并加入缓存，id=" + String.valueOf(userid));
				}
			}
			return user;

		} catch (Exception e) {
			logger.error("获取员工信息异常", e);
		}
		return null;
	}

}
