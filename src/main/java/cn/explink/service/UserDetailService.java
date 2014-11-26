package cn.explink.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import cn.explink.dao.UserDAO;
import cn.explink.domain.User;

public class UserDetailService implements UserDetailsService {

	@Autowired
	UserDAO userDAO;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		List<User> users = userDAO.getUsersByUsername(username);
		if (users.size() == 0) {
			throw new UsernameNotFoundException("沒有找到用戶名{}" + username);
		}
		if (users.size() > 1) {
			throw new RuntimeException("违反了用户名唯一约束");
		}
		User user = users.get(0);
		ExplinkUserDetail explinkUserDetail = new ExplinkUserDetail();
		explinkUserDetail.setUser(user);
		return explinkUserDetail;
	}

}
