# IcyToolset

<!-- Plugin description -->
**IcyToolset** is an IDEA Toolset plugin for own use.
<hr>
TODO LIST:
<ul>
  <li>暂无</li>
</ul>
<!-- Plugin description end -->

Feature list:
- **转换-通用功能：**
  - 支持Base64转换、2/8/10/16进制运算显示、XML/HTML特殊字符转换、Unicode转换、Uri参数特殊字符转换、Json特殊字符转换、时间/Unix时间戳猜测/运算显示。
- **转换-文件功能：**
  - 支持导入导出通过BASE64编/解码的任何文件。
- **转换-大文本分割器：**
  - 针对于以GB为单位的日志文件进行切割工具。
- **加解密-摘要计算功能：**
  - 支持常见摘要算法及文件导入、批量计算、比对计算结果、批量比对的功能。
- **加解密-非对称/对称加密功能：**
  - 支持常见非对称/对称加密算法文本计算。
  - 支持Jasypt的默认规则运算（可能存在加解密结果不正确的问题）。
- **加解密-JWT：**
  - 支持JWT加解密功能（除PS外，提示不支持的加解密算法可能是因为未填写必要字段如公私钥、Body、Issuer）。
- **条码转换器功能：**
  - 支持QR码/条形码/DataMatrix的文本-二维码图片互相转换。
- **格式化文档功能：**
  - 支持Json/XML的缩进/平铺格式化。
- **脚本环境功能：**
  - 支持JavaScript/Python/Lua/Groovy脚本运行环境（安装PythonCore/EmmyLua以支持语言提示）。
- **标志功能：**
  - 纯文本展现大多数的符号标志供复制使用。
- **其他：**
  - 其他功能如显示当前编辑器的JVM系统编码。
- **Actions：**
  - 于`Object a = new Object();`等语句中，针对于标识符a可使用`Alt+Insert`IDEA生成快捷键进行快速生成setXxx()方法。
  - 基于插件设置中的BeanUtils类列表，可在项目窗口右键类文件，扫描出该类是否被这些BeanUtils调用。
  - 可在项目窗口右键项目/类等文件，扫描并分析该项目依赖库的字节码版本与该项目JDK版本是否吻合（满足最低要求）并输出报表。
