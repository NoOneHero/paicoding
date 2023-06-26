package com.github.paicoding.forum.service.user.service.user;

import com.github.paicoding.forum.api.model.enums.NotifyTypeEnum;
import com.github.paicoding.forum.api.model.enums.user.LoginTypeEnum;
import com.github.paicoding.forum.api.model.enums.user.StarSourceEnum;
import com.github.paicoding.forum.api.model.enums.user.UserAIStatEnum;
import com.github.paicoding.forum.api.model.vo.notify.NotifyMsgEvent;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.core.util.TransactionUtil;
import com.github.paicoding.forum.service.user.repository.dao.UserAiDao;
import com.github.paicoding.forum.service.user.repository.dao.UserDao;
import com.github.paicoding.forum.service.user.repository.entity.UserAiDO;
import com.github.paicoding.forum.service.user.repository.entity.UserDO;
import com.github.paicoding.forum.service.user.repository.entity.UserInfoDO;
import com.github.paicoding.forum.service.user.service.RegisterService;
import com.github.paicoding.forum.service.user.service.help.UserPwdEncoder;
import com.github.paicoding.forum.service.user.service.help.UserRandomGenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户注册服务
 *
 * @author YiHui
 * @date 2023/6/26
 */
@Service
public class RegisterServiceImpl implements RegisterService {
    @Autowired
    private UserPwdEncoder userPwdEncoder;
    @Autowired
    private UserDao userDao;

    @Autowired
    private UserAiDao userAiDao;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long registerByUserNameAndPassword(String username, String star, String password, String inviteCode) {
        // 保存用户登录信息
        UserDO user = new UserDO();
        user.setUserName(username);
        user.setPassword(userPwdEncoder.encPwd(password));
        user.setThirdAccountId("");
        user.setLoginType(LoginTypeEnum.USER_PWD.getType());
        userDao.saveUser(user);

        // 保存用户信息
        UserInfoDO userInfo = new UserInfoDO();
        userInfo.setUserId(user.getId());
        userInfo.setUserName(username);
        userInfo.setPhoto(UserRandomGenHelper.genAvatar());
        userDao.save(userInfo);

        // 保存ai相互信息
        UserAiDO userAiDO = new UserAiDO();
        userAiDO.setUserId(user.getId());
        userAiDO.setStarNumber(star);
        userAiDO.setStarType(StarSourceEnum.JAVA_GUIDE.getSource());

        if (inviteCode != null) {
            UserAiDO invite = userAiDao.getByInviteCode(inviteCode);
            if (invite != null) {
                userAiDO.setInviterUserId(invite.getUserId());
                userAiDO.setCondition(2);
            } else {
                userAiDO.setInviterUserId(0L);
                userAiDO.setCondition(0);
            }
        } else {
            userAiDO.setInviterUserId(0L);
            userAiDO.setCondition(0);
        }
        userAiDO.setInviteCode(UserRandomGenHelper.genInviteCode(user.getId()));
        userAiDO.setState(UserAIStatEnum.IGNORE.getCode());
        userAiDao.save(userAiDO);

        processAfterUserRegister(user.getId());
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long registerByWechat(String thirdAccount) {
        // 用户不存在，则需要注册
        // 保存用户登录信息
        UserDO user = new UserDO();
        user.setThirdAccountId(thirdAccount);
        user.setLoginType(LoginTypeEnum.WECHAT.getType());
        userDao.saveUser(user);


        // 初始化用户信息，随机生成用户昵称 + 头像
        UserInfoDO userInfo = new UserInfoDO();
        userInfo.setUserId(user.getId());
        userInfo.setUserName(UserRandomGenHelper.genNickName());
        userInfo.setPhoto(UserRandomGenHelper.genAvatar());
        userDao.save(userInfo);

        // 保存ai相互信息
        UserAiDO userAiDO = new UserAiDO();
        userAiDO.setUserId(user.getId());
        userAiDO.setStarNumber("");
        userAiDO.setStarType(0);
        userAiDO.setInviterUserId(0L);
        userAiDO.setCondition(1);
        userAiDO.setInviteCode(UserRandomGenHelper.genInviteCode(user.getId()));
        userAiDO.setState(UserAIStatEnum.IGNORE.getCode());
        userAiDao.save(userAiDO);

        processAfterUserRegister(user.getId());
        return user.getId();
    }


    /**
     * 用户注册完毕之后触发的动作
     *
     * @param userId
     */
    private void processAfterUserRegister(Long userId) {
        TransactionUtil.registryAfterCommitOrImmediatelyRun(new Runnable() {
            @Override
            public void run() {
                // 用户注册事件
                SpringUtil.publishEvent(new NotifyMsgEvent<>(this, NotifyTypeEnum.REGISTER, userId));
            }
        });
    }
}
