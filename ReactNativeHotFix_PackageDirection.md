# ReactNativeHotFix 打包指南
### 更新方式
#### 版本更新流程图
![图片](https://raw.githubusercontent.com/yangyunfeng666/image/master/reactnative_1.png)
#### 版本回退 
版本回退到以前的一个版本，前提是本地存在以前版本的bundle文件。
必须提供回退的版本号，如果没有回退的版本，会回退到app 打包的版本
#### 全量更新
全量下载一个新的bundle文件，但是图片可以以本地图片为基础增量。
bundle压缩文件存储 index.android.bundle 文件和 drawable-mdpi文件
```
全量更新，包括bundle文件，与比app drawable里面 新增的图片文件。（这里新增的图片指的是在原生app存放的图片上的修改或者新增，因为在热更新后，程序会默认把app里面以assets开头的图片拷贝到sdcard bundle加载路径下，这样减少了全量更新和第一次热更新的图片打包大小）
```
#### 增量更新
需要一个oldversion版本号和新的版本号 和 bundle 压缩包里面的bundle.pat文件和图片文件
```
有2种情况
1.如果你以前有oldversion版本的bundle文件
会把以前版本的bundle文件和pat文件合并生成新版本号的bundle文件，图片资源也会添加到sdacrd的drawable-mdpi里面。
2.没有以前oldversion版本的bundle
会把当前pat文件和app assents目录下的index.android.bundle文件合并成新的版本号的bundle文件，而且以前在drawable下面以assets开头的图片，也会合并到新的sdcard的drawable-mdpi目录下。
```

```flow
st=>start: 开始
e=>end: 更新
ops=>operation: 是否有更新数据
update=>operation:增量

cond=>condition: 是？
updatecond=>condition: 是否回退更新？
allUpdate=>condition: 是否回退更新？
oldUpdate=>condition: 是否有老版本
addUpdate=>condition: 本地增量更新
st->ops->cond->update
cond(yes)->e
cond(no)->updatecond
updatecond(yes)->e
updatecond(no)->allUpdate
allUpdate(yes)->e
allUpdate(no)->oldUpdate
oldUpdate(yes)->e
oldUpdate(no)->addUpdate
addUpdate(yes)->e
```
### 修改代码
修改你的版本代码，这里包括2种情况
```
1.你只修改了reactnative的代码，没有添加图片资源文件
	如果是这种情况，你只需要打patch包或者打增量包即可
2.你即修改了图片文件又新增了图片，或者你替换了原来的图片
	如果是这种情况，如果你是打增量包，你需要修改ractnative的源码添加图片变量和修改加载图片资源逻辑的方法，如下**修改了图片资源打包**部分
	但是如果你是打的是全量包，你不需要去修改源码部分，直接把新添加的图片放到drawable-mdpi文件夹即可
```

### 修改了图片资源打包
1.首先修改你的js文件
2.按照步骤修改react_native 源码，在源码里面修改方法和添加增加的图片文件变量，然后在生成增量包
需要修改node_modules / react-native / Libraries / Image /AssetSourceResolver.js 文件
```

defaultAsset(): ResolvedAssetSource {
  if (this.isLoadedFromServer()) {
    return this.assetServerURL();
  }

  if (Platform.OS === 'android') {
    return this.isLoadedFromFileSystem()
      ? this.drawableFolderInBundle()
      : this.resourceIdentifierWithoutScale();
  } else {
    return this.scaledAssetURLNearBundle();
  }
}

```
defaultAsset方法中根据平台的不同分别执行不同的图片加载逻辑。重点我们来看android platform：
drawableFolderInBundle方法为在存在离线Bundle文件时，从Bundle文件所在目录加载图片。resourceIdentifierWithoutScale方法从Asset资源目录下加载。由此，我们需要修改isLoadedFromFileSystem方法中的逻辑。
步骤
1.在AssetSourceResolver.js中增加增量图片全局名称变量
```
'use strict';

export type ResolvedAssetSource = {|
  +__packager_asset: boolean,
  +width: ?number,
  +height: ?number,
  +uri: string,
  +scale: number,
|};

import type {PackagerAsset} from 'AssetRegistry';


var patchImgNames = ''; // 声明全局变量在这里
```
2.修改isLoadedFromFileSystem 把
```
//  isLoadedFromFileSystem(): boolean {
//    return !!(this.jsbundleUrl && this.jsbundleUrl.startsWith('file://'));
//  }

  isLoadedFromFileSystem(): boolean {
    var imgFolder = getAssetPathInDrawableFolder(this.asset);
    var imgName = imgFolder.substr(imgFolder.indexOf("/") + 1);
    var isPatchImg = patchImgNames.indexOf("|"+imgName+"|") > -1;
    return !!this.bundlePath && isPatchImg;
  }
```
patchImgNames是增量更新的图片名称字符串全局缓存，其中包含所有更新和修改的图片名称，并且以 “|”隔开。当系统加载图片时，如果在缓存中存在该图片名，证明是我们增量更新或修改的图片，所以需要系统从Bundle文件所在目录下加载。否则直接从原有Asset资源加载，所以把原来的方法去掉，改成现在的方法
3.每当有图片增量更新，修改patchImgName，例如images_ic_1.png和images_ic_2.png为增量更新或修改的图片
 ```
 // 全局缓存 
var patchImgNames = '|image_images_ic_2.png|';
//这里我们我们的图片是image目录下的images_ic_2.png 因为最终打包生成的文件在drawable-mdpi下变成了image_images_ic_2.png，所以这里也是用image_images_ic_2.png，如果是多张图，需要|分割开
 ```
 
 生成bundle目录时，图片资源都会放在统一目录下（drawable-mdpi），如果引用图片包含其它路径，例如require(“./img/test1.png”)，图片在img目录下，则图片加载时会自动将img目录转换为图片名称：”img_test1.png”，即图片所在文件夹名称会作为图片名的前缀。此时图片名配置文件中的名称也需要声明为”img_test1.png”，例如：" | img_test1.png | img_test2.png | "

4.然后重新打包：并且重新生成把bundle.pat和drawable-mdpi文件压缩成bundle.zip文件。
**注意** 我们要把生成的pat文件和drawable-mdpi都放在bundle文件夹里面然后压缩，而如果没有drawable-mdpi图片资源文件时候，也是放到bundle 文件下压缩，压缩成bundle.zip文件
这里我们把bundle.zip 文件存在在git 上面地址是
```
https://raw.githubusercontent.com/yangyunfeng666/image/master/bundle.zip
```
当你需要发布有图片的bundle包后，如果你需要打不需要添加图片的包，需要你把上述修改AssetSourceResolver.js 代码修改过来


### 生成pat 文件
react-dispatch.jar 需要三个参数 
```
java -jar react-dispatch.jar old/index.android.bundle new/index.android.bundle zip/bundle/bundle.pat
1.第一参数是 旧的bundle文件的目录
2.第二参数是 新的bundle文件的目录
3.第三参数是 输出patch文件的目录 输出pat文件名称必须是bundle.pat 
```
![](https://raw.githubusercontent.com/yangyunfeng666/image/master/reactnative_2.png)
然后把生成的bundle.pat文件放到bundle文件夹中，然后压缩bundle文件，变成bundle.zip文件，这个才是更新的压缩文件
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
3.再测试全量更新到1.0.2版本，然后进入React查看效果
4.再测试以1.0.1版本跟新到1.0.3版本，然后进入React查看效果
5.再测试回退到1.0.1版本，然后进入React查看效果



	

