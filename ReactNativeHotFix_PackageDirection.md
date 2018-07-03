# ReactNativeHotFix 打包指南
### 更新方式
#### 版本更新流程图
![图片](https://raw.githubusercontent.com/yangyunfeng666/image/master/reactnative_1.png)
#### 版本回退
版本回退到以前的一个版本，前提是本地存在以前版本的bundle文件。
必须提供回退的版本号，如果没有回退的版本，会回退到app 打包的版本
#### 全量更新
全量下载一个新的bundle文件，但是图片可以以本地图片为基础增量。
bundle压缩文件存储 index.android.bundle 文件和 drawable-xhdpi文件
```
全量更新，包括bundle文件，与比app drawable里面 新增的图片文件。（这里新增的图片指的是在原生app存放的图片上的修改或者新增，因为在热更新后，程序会默认把app里面以assets开头的图片拷贝到sdcard bundle加载路径下，这样减少了全量更新和第一次热更新的图片打包大小）
```
#### 增量更新
需要一个oldversion版本号和新的版本号 和 bundle 压缩包里面的bundle.pat文件和图片文件
```
有2种情况
1.如果你以前有oldversion版本的bundle文件
会把以前版本的bundle文件和pat文件合并生成新版本号的bundle文件，图片资源也会添加到sdacrd的drawable-xhdpi里面或者其他文件里面，跟你在Application初始化有关系默认是drawable-xhdpi。
2.没有以前oldversion版本的bundle
会把当前pat文件和app assents目录下的index.android.bundle文件合并成新的版本号的bundle文件，而且以前在drawable下面以assets开头的图片，也会合并到新的sdcard的drawable-xhdpi目录下。
```
### 修改代码
修改你的版本代码，这里包括2种情况
```
1.你只修改了reactnative的代码，没有添加图片资源文件如果是这种情况，你只需要打patch包或者打增量包即可
2.你即修改了图片文件又新增了图片，或者你替换了原来的图片,如果是这种情况，直接把新添加或者修改的图片放到drawable-mdpi文件夹或者drawable-xhdpi,这和你在application初始化的图片文件目录有关系,但是如果你是打的是全量包，放入index.android.bundle.js文件，否则放入bundle.pat增量包
```

### 生成pat 文件
react-dispatch.jar 需要三个参数
```
java -jar react-dispatch.jar old/index.android.bundle new/index.android.bundle zip/bundle/bundle.pat
1.第一参数是 旧的bundle文件的目录
2.第二参数是 新的bundle文件的目录
3.第三参数是 输出patch文件的目录 输出pat文件名称必须是bundle.pat
```
![](https://raw.githubusercontent.com/yangyunfeng666/image/master/reactnative_2.png)
然后把生成的bundle.pat文件放到bundle文件夹中，如果你有新增的图片或者修改的图片，请把新增的图片放入比如drawable-xhdpi中，当然如果你在Application里面初始化的图片打包地址是drawable-mdip，那么就放在drawable-mdip中，一定要一致，不然你的图片是显示不了的。drawable-xhdpi放入bundle文件夹，在然后压缩bundle文件，变成bundle.zip文件，这个才是更新下载的压缩文件
react-dispatch.jar 的下载地址是：
[下载](https://raw.githubusercontent.com/yangyunfeng666/image/master/react-dispatch.jar)
react-dispatch.jar 里面的java 代码如下
main.java
```

public class MainClass {

	public static void main(String[] args) {
		if(args.length<3){
			return ;
		}
		String outPatchPath = args[2];
		if(!outPatchPath.endsWith("pat")){
			System.out.println("输出文件不是pat文件类型结尾");
			return;
		}
		String oldBundlePath = getStringFromPat(args[0]);
		String newBundlePath = getStringFromPat(args[1]);
		// 对比
		diff_match_patch dmp = new diff_match_patch();
		LinkedList<Diff> diffs = dmp.diff_main(oldBundlePath, newBundlePath);
		// 生成差异补丁包
		LinkedList<Patch> patches = dmp.patch_make(diffs);
		// 解析补丁包
		String patchesStr = dmp.patch_toText(patches);
		try {
		    // 将补丁文件写入到某个位置
		    Files.write(Paths.get(outPatchPath), patchesStr.getBytes());
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	}


	 public static String getStringFromPat(String patPath) {
		    FileReader reader = null;
		    String result = "";
		    try {
		        reader = new FileReader(patPath);
		        int ch = reader.read();
		        StringBuilder sb = new StringBuilder();
		        while (ch != -1) {
		        sb.append((char)ch);
		        ch  = reader.read();
		        }
		        reader.close();
		        result = sb.toString();
		    } catch (FileNotFoundException e) {
		        e.printStackTrace();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		     return result;
		}
}
```
[diff_match_patch.java](https://raw.githubusercontent.com/yangyunfeng666/image/master/diff_match_patch.java)下载地址
### 测试
1.进入react界面后，直接点击进入React查看效果，这个界面就是app到包的效果
2.先测试 没有旧版本更新到1.0.1，然后进入React查看效果
3.再测试全量更新到1.0.3版本，然后进入React查看效果
4.再测试以1.0.1版本跟新到1.0.2版本，然后进入React查看效果
5.再测试回退到1.0.1版本，然后进入React查看效果
### 注意
1.图片资源，在现在版本的开发，只允许3中文件夹里面的图片被使用，components开头的文件夹和node开头的文件和assets里面的文件，因为在打包时候只能移动这个三个文件夹开头的图片2x的图片资源到sdcard，这跟app端的代码写死的。如果需要修改，需要前端app初始化移动文件夹时候添加新的文件夹前缀。
2.所有rn使用的本地图片资源，在文件夹里面，必须是xxx@2x.png格式的。因为这样打包时候会打到drawable-xhdpi文件夹里面，而现在，app端的初始化也是drawable-xhdpi文件夹，当然可以前端代码可以修改。
### 打包步骤
1.修改代码
2.如果有图片资源修改，确定图片资源，是否在三个文件夹里面，而且xxx@2x.png 格式
3.增量更新还是，全量更新，如果是增量更新，通过jar包打增量包，生成bundle.pat文件，如果是全量更新，直接放index.android.bundle.js文件，然后是否更新图片，更新图片都放到drawable-xhdpi里面，生成bundle.zip文件
4.放到服务器，调试下载
5.发布版本

