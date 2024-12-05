<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# IcyToolset Changelog

## [Unreleased]
### 调整

## [1.16]
### 调整
- 更新至2024.3
- 脚本执行工具支持IDE进度条取消事件

## [1.15]
### 调整
- 代码生成菜单生成Setter功能添加对Map实例的支持，但数据来源与去向实例不能均为Map。
- 哈希计算添加文件夹支持。可将计算来源文件夹导出至清单文件，后续使用另一个目录与该清单文件做哈希比对。
- 哈希计算改为后台方式计算，防止大量文件或大文件计算导致冻结UI。
- 添加SM2密钥类型（Base64/Hex）切换、对SM2密钥长度固定限制添加UI反馈。
- 项目视图中右键增加依赖类文件Java版本分析功能，如批量识别该项目中各模块依赖包中类文件是否能够使用模块SDK版本所加载，并生成报告。

## [1.14]
### 调整
- 更新依赖版本
- 脚本执行器Groovy支持从项目加载依赖（包含编译后的项目输出）
- 脚本执行器编辑器控件支持Ctrl+ENTER快捷键执行脚本（忽略自动执行单选）
- 编解码窗口在解码过程中若勾选了多行处理且实际只处理了一行、并且是含多行文本的内容则会自动关闭多行处理

## [1.13]
### 调整
- JWT编解码功能（不支持PS算法的编码）

## [1.12]
### 调整
- 数据矩阵（DataMatrix）添加参数以支持ECI字符。
- QR生成添加个性化生成如高位色/遮罩图、低位色/遮罩图、Logo。
- 非对称生成密钥改为切换式常见长度而非固定1024。
- 更新依赖。

## [1.11]
### 调整
- 重构Base64转换单/多文件逻辑，不再需要按住CTRL键。
- 为几个功能增加执行后打开操作文件的路径功能（仅限Windows）。

## [1.10]
### 调整
- 格式化功能改用编辑器气泡的方式提示错误信息。
- 脚本功能改为异步执行。
- BeanUtils引用查询改为异步执行。
- BeanUtils引用查询提供配置功能（位于设置->工具->Icy Toolset）。

## [1.9]
### 调整
- 生成菜单中增加了Setter生成功能，对实例对象打开生成菜单（默认Alt+Insert）即可调用。
- Python脚本引擎执行时按连续两行分隔符进行分割脚本并批量执行，因脚本引擎仅支持单句输出并支持上下文。
- 脚本执行窗口恢复编辑器->结果窗口的滚动条同步功能。
- 项目菜单中对java文件右键及编辑器内对实例/类型打开意图菜单（默认Alt+Enter）可以调用“寻找引用自BeanUtils…”功能，目前可以寻找该实例/类型被类名含有BeanUtil或TypeUtil的类静态方法调用的位置。

## [1.8]
### 调整
- 2023.1兼容性更新。
- 调整了因部分界面文本域会根据文本大小变化而影响到操作体验的问题。
- 对格式化功能与脚本功能支持IDEA Editor控件（语义高亮识别、提示）。
- 脚本功能支持IDEA VFS存储（应用级别）。
- 工具窗口顶层TAB改用工具窗口自带的TAB模式。
- 对界面及程序提示支持I18n。

## [1.7]
### 调整
- 修复条码解码功能未根据条码类型单选进行处理的问题。
- 非对称加解密功能中添加Jasypt默认加解密模式。

## [1.6]
### 调整
- 大文本分割工具控件屏蔽字符修复。
- 升级依赖版本。
- 支持四种ScriptEngine脚本执行（js/python/lua/groovy）并支持运行在JDK17下的IDEA运行该功能。
- 针对IDEA新UI改变了文本框的描边。
- 转换-文件功能支持选择文件夹（按住Ctrl点击UI控件）批量转换。

## [1.5]
### 调整
- 大文本分割工具控件屏蔽字符修复。

## [1.4]
### 调整
- 升级依赖版本。
- 修复哈希计算中导入文件计算错误的问题。
- 为通用转换功能和三个加解密功能加入一键清空按钮。
- 添加大文本分割工具。
- 格式化JSON/XML支持平铺模式。
- 批量哈希文件计算/生成工具（支持自动生成POM文件，用于导入jar包生成.m2仓库）。

## [1.3]
### 调整
- 调整文本域UI设置。

## [1.2]
### 调整
- 调整部分UI大小。
- 将可关联的水平、垂直滚动条进行滚动事件关联。

## [1.1]
### 调整
- 修复转换-文件中使用文本框解码为文件功能发生堆栈溢出的问题。
- 调整全部的保存为文件功能对话框初始目录，设计为上次保存的路径或导入文件操作的路径。

## [1.0]
### 新增
- **转换-通用功能：**
  - 支持Base64转换、2/8/10/16进制运算显示、XML/HTML特殊字符转换、Unicode转换、Uri参数特殊字符转换、Json特殊字符转换、时间/Unix时间戳猜测/运算显示。
- **转换-文件功能：**
  - 支持导入导出通过BASE64编/解码的任何文件。
- **加解密-摘要计算功能：**
  - 支持常见摘要算法及文件导入、批量计算、比对计算结果、批量比对的功能。
- **加解密-非对称/对称加密功能：**
  - 支持常见非对称/对称加密算法文本计算。
- **条码转换器功能：**
  - 支持QR码/条形码/DataMatrix的文本-二维码图片互相转换。
- **格式化文档功能：**
  - 支持Json/XML的美化格式化。
- **脚本环境功能：**
  - 支持JavaScript脚本运行环境。
- **标志功能：**
  - 纯文本展现大多数的符号标志供复制使用。
- **其他：**
  - 其他功能如显示当前编辑器的JVM系统编码。

[Unreleased]: https://github.com/IceLitty/IcyToolset/compare/v1.16...HEAD
[1.16]: https://github.com/IceLitty/IcyToolset/compare/v1.15...v1.16
[1.15]: https://github.com/IceLitty/IcyToolset/compare/8e63c82e813361165431735668aa78f8369acdef...v1.15
[1.14]: https://github.com/IceLitty/IcyToolset/compare/d96bf5bcc2b849cc3840ff65bfbecd9b47b343c5...8e63c82e813361165431735668aa78f8369acdef
[1.13]: https://github.com/IceLitty/IcyToolset/compare/2fdc7f2c5a7cdf77e4dbe4908787c7aaa057be7f...d96bf5bcc2b849cc3840ff65bfbecd9b47b343c5
[1.12]: https://github.com/IceLitty/IcyToolset/compare/4fb1e3846a62380e777303cd7139ae1b8f596836...2fdc7f2c5a7cdf77e4dbe4908787c7aaa057be7f
[1.11]: https://github.com/IceLitty/IcyToolset/compare/7a4de810a80c37ac63d170416ec09662931452ba...4fb1e3846a62380e777303cd7139ae1b8f596836
[1.10]: https://github.com/IceLitty/IcyToolset/compare/3f77067910376341d1a35e22f107bfba0461e844...7a4de810a80c37ac63d170416ec09662931452ba
[1.9]: https://github.com/IceLitty/IcyToolset/compare/234ee49ff4594a047f7a2815c9436540cc28e5dc...3f77067910376341d1a35e22f107bfba0461e844
[1.8]: https://github.com/IceLitty/IcyToolset/compare/a27c601488d196d48c43d0f5c0ea11df570a530d...234ee49ff4594a047f7a2815c9436540cc28e5dc
[1.7]: https://github.com/IceLitty/IcyToolset/compare/52ce9340dff52156910041511a03eb1df1548cb6...a27c601488d196d48c43d0f5c0ea11df570a530d
[1.6]: https://github.com/IceLitty/IcyToolset/compare/af33ab7922ced11882f382fd74253ec2f36e32ec...52ce9340dff52156910041511a03eb1df1548cb6
[1.5]: https://github.com/IceLitty/IcyToolset/compare/5241789f5c241273a020d27f544dfe6e42b86e57...af33ab7922ced11882f382fd74253ec2f36e32ec
[1.4]: https://github.com/IceLitty/IcyToolset/compare/2e6e9b122c14c6faf13cced221fbd7231f2f4228...5241789f5c241273a020d27f544dfe6e42b86e57
[1.3]: https://github.com/IceLitty/IcyToolset/compare/e745c5bfae07d5b5c688061c0af551afd521bfbe...2e6e9b122c14c6faf13cced221fbd7231f2f4228
[1.2]: https://github.com/IceLitty/IcyToolset/commit/e745c5bfae07d5b5c688061c0af551afd521bfbe
[1.1]: https://github.com/IceLitty/IcyToolset/commit/e745c5bfae07d5b5c688061c0af551afd521bfbe
[1.0]: https://github.com/IceLitty/IcyToolset/commit/e745c5bfae07d5b5c688061c0af551afd521bfbe
