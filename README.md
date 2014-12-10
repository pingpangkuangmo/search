search
======

主要功能：对于查询进行配置化

1 对select语句查询出的平铺的结果展示进行聚合，使其具有父子关系

2 对select语句查询出的结果进行格式化，如true、false改为是、否

3 对于查询条件可以随意增添，支持类似mongodb的and or

4 对于查询可以直接指定表之间的连接路径，如 organization join organization_product_lines join product_line

改进：

1 画出整体流程，分清责任机制，该扩展扩展

2 集成问题：xml配置文件、编码配置

查询体配置说明：
1 columns：用于指定你想要查询哪些表的哪些字段（必选）
功能：
（1） 多个表字段名重复时可以指定别名 如 "app.name as appName"
（2）同时可以指定返回结果为一对一的关系，即如下形式，外层是app的内容，app中含有一个product
       <pre><code>
       	{
            "status": "正常",
            "id": 15423,
            "english_name": "toolmon",
            "product": {
                "englishName": "sdjsd",
                "name": "sdgfdgv",
            },
            "productLine": {
                "englishName": "PM1",
                "name": "sefsd",
            }
        }
	</code></pre>
    columns的写法为：
    <pre><code>
    [ 
    	"app.id","app.english_name","status.name as status",
	    "product.name as `product@mapname`","product.english_name as `product@mapenglishName`",
	    "product_line.name as `productLine@mapname`","product_line.english_name as `productLine@mapenglishName`"
    ]
    </code></pre>
    
       <pre>  以  product.name as `product@mapname` 为例，product.name起别名为`product@mapname`（一定要加上``，注意不是单引号），@map前面表示该字段为product属性的一部分
         后面表示在product属性中显示的名称。所以在结果中会有product属性中含有name属性。同理product.english_name as `product@mapenglishName`则表示在product属性中
         会有一个englishName属性。对于productLine同理</pre>
         
（3）可以指定一对多的关系，如下形式，外层是organization信息，每个organization含有一个product_line list集合
   		{
            "id": 63,
            "name": "sdfer",
            "productLines": [
                {
                    "id": 162,
                    "name": "wefer"
                },
                {
                    "id": 163,
                    "name": "werfe"
                },
                {
                    "id": 164,
                    "name": "werew"
                }
            ]
        }
      columns和groupColumns配置如下：
        "columns":[ 
        	"organization.id","organization.name",
		  	"product_line.id as `productLines@listid`","product_line.name as `productLines@listname`"
		],
		"groupColumns":["organization.id"]
		
                    以product_line.id as `productLines@listid`为例，product_line.id起别名为`productLines@listid`（一定要加上``，注意不是单引号），
       @list之前的内容productLines表示在外层organization中有一个productLines属性，@list之后的内容id表示每一个productLine中显示的属性名
                  使用@list标签则意味着要对结果进行聚合，所以必须给出在什么情况下organization是同一个organization，仍需要配置groupColumns字段，表示
                  当两个organization的groupColumns都相同的话，则为同一个organization。

2   params字段：用于加入查询条件（可选）
           （1）最简单的形式如下：
          "params":{
		    			"app.id":123
		  			}
		  表示条件为app.id=123，即默认是=的操作
	（2  还支持更多的操作，如 >、>= < <= != is like等操作，使用如下：
		  "params":{
		    			"app.id@>=":123,"app.name@like":"%应用%"
		  			}
		 表示条件为app.id>=123,app.name含有"应用"。它们的使用必须在前面配上@符号作为标记
		 
           （3）还支持 in 和 not in 操作，案例如下：
		  "params":{
		    			"app.id@in":[123,345,232],"app.name@notIn":["ass","assa","sdwe"]
		  			}
		  表示app.id必须在[123,345,232]之中，并且app.name必须不能在["ass","assa","sdwe"]之中
		
	（4）还支持时间的大于小于等操作，如 time>= time> time< time<=,案例如下
		"params":{
		    		"app.create_time@time>=":"2014-10-1","app.create_time@time<=":"2014-12-1"
		  		}
		表示app的create_time在"2014-10-1"和"2014-12-1"范围之内，可以随意增添条件，同时还支持如下形式：
		"params":{
		    		"app.create_time@time>=":145232342
		  		}
		145232342表示至今的秒数
           （5）还支持and or 操作，上述条件组合默认都是and的关系，使用案例如下：
       "params":{
		    		"$or":{"app.id":10431,"app.name@like":"%Product%"},
		    		"app.age@<":12237 
		  		}
		app.id和app.name是or的关系，然后它们与app.age是and的关系
3  format 对返回结果的某些字段的数值进行格式化（可选），案例如下：
	"format":[
		  			{
		  				"ruleType":"map",
		  				"column":"appImportance",
		  				"ruleBody":{
		  					"true":"是",
		  					"false":"否"
		  				}
		  			},
		  			{
		  				"ruleType":"regex",
		  				"column":"name",
		  				"ruleBody":{
		  					"regex":"lg",
		  					"replacement":"lg123"
		  				}
		  			}
		  	]
	以第一个为例，ruleType表示你要使用哪种形式的格式化，内置只有两个值map和regex,column表示要对哪个字段进行格式化，
	当ruleType为map时，表示appImportance属性对应的值如果为true，则要替换成是，若为false，则要替换成否
	当ruleType为regex时，表示name属性符合regex的部分要替换成replacement对应的值，即name中的lg都要替换成lg123，format属性中可以对多个字段的值进行格式化

4   tablesPath ：用于指定表之间的连接路径（可选）
    
