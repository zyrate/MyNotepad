# MyNotepad
## 项目简介
MyNotepad是我用原生Java编写的简易记事本。可自定义高亮、记笔记、更改字体、
进入代码模式、查找替换、字数统计等实用功能。正在不断完善ing...
## 使用方法
- 导入到IDEA中，运行AppMain中的启动方法
- 开始运行后会开启一个窗口(即打开一次.jar文件)，之后关闭
- 将本项目的default.highlight文件复制到C:\NotepadData\highlights文件夹下
- 再次运行(打开.jar文件)即可使用最重要的高亮功能
## 高亮策略介绍
写MyNotepad的初衷就是希望能够在记笔记的时候像写代码一样，随时有高亮提示，
而不是像富文本编辑器那样。虽然MarkDown能够写出很漂亮笔记，
但是我认为它的表现力有些局限，不能根据自己的想象记出丰富多彩的笔记（或许是我自己的问题~）。

MyNotepad的高亮有三种类型：`KEYWORD 关键字`,`ALL_LINE 整行`和`PART 区域`。以Java代码的高亮为例，
`int, double, char`等单词的高亮属于KEYWORD，`//`行注释的高亮属于ALL_LINE,
`/**/`块注释的高亮属于PART。