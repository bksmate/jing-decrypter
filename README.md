# jing-decrypter
Some function to decrypt something.

环境要求: JDK 1.8
---

Email: 357166513@qq.com
---

闲谈
---
最近想要拿解包到的舟游的小人去玩一下, 但是在刚入手的时候遇到了很多的问题, 最大的问题就是使用Spine官方的对.skel(龙骨文件)的API去反编译舟游的.skel文件时会报错.<br>
东查查西查查, 折腾了蛮久的, 最后瞄准了`舟游Wiki`以及B站另外一个大佬@`凊弦凝绝FrozenString`对舰B的.skel反编译的代码.<br>
结合着边理解边改写, 最后总算是搞定了.<br>
虽说是搞定了, 但其实还是不理解这个二进制文件的生成规则是哪里来的, 原谅我只是个搞Web的代码工, 安卓和U3D方面真的超纲了……<br>
感谢大佬们的肩膀, 能让我完成这部分代码.<br>
有任何问题欢迎邮箱反馈. <br>

食用方法:
---
* source code<br>
直接拉到IDEA里就可以跑了.<br>
`org.jing.decrypter.skeleton2Json.Arknights`就是给舟游用的.<br>
`Arknights decrypter = new Arknights(file);`<br>
`decrypter.decrypter();`<br>
`JSONObject resultJson = decrypter.getRetJson();`<br>
* .jar<br>
双击根目录的`exe.bat`, 然后选择`1`就是用于舟游的模块, 接下来去选择文件夹或者文件(支持多选)即可.<br>
但是注意, 如果一次反编译多个文件, 其中的某个文件失败报错会导致后续的文件都失败, 如果不爽这个可以自己改一下源码, 加个错误catch不要throw/publish即可.<br>

时间线
---
===[2020-07-15]===<br>
第一次上传, 公开.<br>
