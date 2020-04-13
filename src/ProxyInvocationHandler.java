import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

// 自动生成代理类
public class ProxyInvocationHandler implements InvocationHandler{
    //被代理的接口
    private  Object target;

    public void setTarget(Object target) {
        this.target = target;
    }

    // 生成得到代理类
    public Object getProxy(){
        return Proxy.newProxyInstance(this.getClass().getClassLoader(),
                target.getClass().getInterfaces(), this);
    }

    // 处理代理实例，并返回结果；
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = method.invoke(target, args);
        return result;
    }

    // 加入生成日志方法
    public void  log(String msg){
        System.out.println("执行了"+msg+"方法");
    }
}
/*  如何使用
public class Client{
    public static void main(String[] args) {
        // 真实的角色
        UserServiceImpl userService = new UserServiceImpl();
        //代理角色，不存在
        ProxyInvocationHandler pih = new ProxyInvocationHandler();
        pih.setTarget(userService); // 设置要代理的对象
        UserService proxy = (userService) pih.getProxy();
        proxy.query(); // 使用UserService 存在的方法
    }
}
 */