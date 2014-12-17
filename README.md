search
======

# 主要功能：对于查询进行配置化

1. 对select语句查询出的平铺的结果展示进行聚合，使其具有父子关系

2. 对select语句查询出的结果进行格式化，如true、false改为是、否

3. 对于查询条件可以随意增添，支持类似mongodb的and or

4. 对于查询可以直接指定表之间的连接路径，如 organization left join product_line

# 改进：

1. 加入解析缓存

# 测试案例

URL: http://192.168.83.240:18080/api/search
请求方法: POST请求

请求体如下内容：

1.  获取创建时间大于某个值的所有APP信息，其中APP中包含组织、产品线、产品信息

		{
		  "columns":["app.id","app.app_id","app.name as  appName","app.english_name","app.create_time","status.name as status",
		  				"app.description","app_container.name as appContainer","app_category.name as appCategory","app_importance.name as appImportance", 
		  				"organization.name as `organization@mapname`","organization.english_name as `organization@mapenglishName`","organization.code as `organization@mapcode`",
		  				"product_line.name as `productLine@mapname`","product_line.english_name as `productLine@mapenglishName`","product_line.code as `productLine@mapcode`",
		  				"product.name as `product@mapname`","product.english_name as `product@mapenglishName`","product.code as `product@mapcode`"
		  				],
		  "params":{
		    			"app.create_time@time>=":"2014-11-1"
		  			},
		  "order_by":"app.id desc"
		}

2.  查询条件and、or的测试

		{
		  "columns":["app.id","app.app_id","app.name as  appName","app.english_name","app.create_time"
		  				],
		  "params":{
		    			"$or":{"app.id":10431,"app.name@like":"%Product%"},
		    			"app.id@<":12237 
		  			},
		  "order_by":"app.id desc"
		}

3.	tablesPath测试

		{
		  "columns":["app.id","app.app_id","app.name as  appName","app.english_name","app.create_time",
		  				"status.name as status"
		  				],
		  "params":{
		    			"$or":{"app.id":10431,"app.name@like":"%Product%"}
		  			},
		  "order_by":"app.id desc",
		  "tablesPath":"app left join status"
		}
		
4.	获取组织下的pool

		{
		  "columns":[ "organization.id","organization.name","organization.english_name as englishName","organization.code",
		  				"pool.name as `pools@listname`","pool.pool_id as `pools@listpoolId`","pool.app_type as `pools@listappType`","pool.importance as `pools@listimportance`",
		  				"pool.pool_type as `pools@listpoolType`","pool.self_support as `pools@listselfSupport`","pool.description as `pools@listdescription`"
		  			],
		  "params":{
		    			
		  			},
		  "order_by":"organization.id desc",
		  "groupColumns":["organization.id"]
		}

5.	更进一步简化columns配置，如获取所有组织下的产品线、产品、应用
   
		{
		  	"entityColumns":["organization","productLines@listproduct_line","products@listproduct","apps@listapp"],
		  	"tablesPath":"organization left join product_line left join product left join app left join app_category left join app_container left join app_importance"
		}
	
6.	更进一步简化，简化中间表和附属表
		
		获取所有组织下的所有产品线、产品、应用
		
		
		{
	  		"entityColumns":["organization","productLines@listproduct_line","products@listproduct","apps@listapp"],
	  		"tablesPath":"organization left join product_line left join product left join app"
		}
		
		
		获取所有应用的信息，包括产品、产品线、组织
		
		
		{
	  		"entityColumns":["app","product@mapproduct","productLine@mapproduct_line","organization@maporganization"],
	  		"tablesPath":"organization left join product_line left join product left join app"
		}
		
		再如：
		
		
		获取所有产品线的产品和应用信息
		
		
		{
	  		"entityColumns":["product_line","products@listproduct","apps@listapp"],
	  		"tablesPath":"product_line left join product left join app "
		}
		
		
		获取所有应用的信息，包括产品和产品线
		
		
		{
	  		"entityColumns":["app","product@mapproduct","productLine@mapproduct_line"],
	  		"tablesPath":"product_line left join product left join app"
		}
		

# 查询体配置说明：
### 1. columns：用于指定你想要查询哪些表的哪些字段（必选）


   (1)  多个表字段名重复时可以指定别名 如 "app.name as appName"

   (2) 同时可以指定返回结果为一对一的关系，即如下形式，外层是app的内容，app中含有一个product

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
	    columns的写法为：
	    [ 
	    	"app.id","app.english_name","status.name as status",
		    "product.name as `product@mapname`","product.english_name as `product@mapenglishName`",
		    "product_line.name as `productLine@mapname`","product_line.english_name as `productLine@mapenglishName`"
	    ]
   
                       以  product.name as `product@mapname` 为例，product.name起别名为`product@mapname`（一定要加上``，
		注意不是单引号），@map前面表示该字段为product属性的一部分后面表示在product属性中显示的名称。
		所以在结果中会有product属性中含有name属性。同理product.english_name as `product@mapenglishName`,
		则表示在product属性中会有一个englishName属性。对于productLine同理
         
（3）可以指定一对多的关系，如下形式，外层是organization信息，每个organization含有一个 product_line list集合

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
		
                       以product_line.id as `productLines@listid`为例，product_line.id起别名为`productLines@listid`,
		（一定要加上``，注意不是单引号）， @list之前的内容productLines表示在外层organization中有一个
		productLines属性，@list之后的内容id表示每一个productLine中显示的属性名使用@list标签则意味着要对结果进行聚合，
		所以必须给出在什么情况下organization是同一个organization，仍需要配置groupColumns字段，表示
                       当两个organization的groupColumns都相同的话，则为同一个organization。

### 2  params字段：用于加入查询条件（可选）

            （1） 最简单的形式如下：
          "params":{
		    			"app.id":123
		  			}
		  表示条件为app.id=123，即默认是=的操作

	（2)  还支持更多的操作，如 >、>= < <= != is like等操作，使用如下：

		  "params":{
		    			"app.id@>=":123,"app.name@like":"%应用%"
		  			}

		 表示条件为app.id>=123,app.name含有"应用"。它们的使用必须在前面配上@符号作为标记
		 
           （3） 还支持 in 和 not in 操作，案例如下：
		  "params":{
		    			"app.id@in":[123,345,232],"app.name@notIn":["ass","assa","sdwe"]
		  			}
		  表示app.id必须在[123,345,232]之中，并且app.name必须不能在["ass","assa","sdwe"]之中
		
	（4） 还支持时间的大于小于等操作，如 time>= time> time< time<=,案例如下
		"params":{
		    		"app.create_time@time>=":"2014-10-1","app.create_time@time<=":"2014-12-1"
		  		}
		表示app的create_time在"2014-10-1"和"2014-12-1"范围之内，可以随意增添条件，同时还支持如下形式：
		"params":{
		    		"app.create_time@time>=":145232342
		  		}
		145232342表示至今的秒数
    （5） 还支持and or 操作，上述条件组合默认都是and的关系，使用案例如下：
       "params":{
		    		"$or":{"app.id":10431,"app.name@like":"%Product%"},
		    		"app.age@<":12237 
		  		}
		app.id和app.name是or的关系，然后它们与app.age是and的关系

### 3  format 对返回结果的某些字段的数值进行格式化（可选），案例如下：

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
	以第一个为例，ruleType表示你要使用哪种形式的格式化，内置只有两个值map和regex,
	column表示要对哪个字段进行格式化，当ruleType为map时，表示appImportance属性对应的值如果为true，
	则要替换成是，若为false，则要替换成否,当ruleType为regex时，表示name属性符合regex的部分要替换成replacement对应的值，
	即name中的lg都要替换成lg123，format属性中可以对多个字段的值进行格式化

### 4   tablesPath ：用于指定表之间的连接路径（可选）

	对于表之间的连接，只需要配置基础的信息，即两个表之间的连接关系。这个需要配置文件，默认是在类路径下baseRelation目录下的所有文件（都会读取），
	格式如下：
	a,b,a.id=b.a_id
	每一行配置一个简单地两表连接，a表和b表，他们的连接关系是a.id=b.a_id
	
	（1）有了这些基础连接信息，就可以对随意给出的几个表使用算法推断出他们的连接关系，这需要一个算法模块
	（2）即使是算法自动推断出他们的连接关系，有时这种连接关系也不一定就是我们所想要的关系，所以又有另一种做法就是，直接指定连接路径
	tablesPath就是针对第二种形式的，如 "tablesPath":"a left join b join c"
	表示此次查询的表之间的连接是a-》b-》c，然后会根据基础的两表之间的连接关系得到最终的连接关系。
	基础信息配置如下:
	a与b的连接关系是a.id=b.a_id
	b与c的连接关系是b.id=c.b_id
	所以最终得到的连接关系是:
	a left join b on a.id=b.a_id
	join c on b.id=c.b_id
	
	tablesPath支持的情况有：
	（1） join、left join、right join
	（2）当tablesPath为 a join b join c,可能真实的连接情况是a与b有关系，a与c有关系，所以在处理join c的时候会依次向前找，看看那个与c有关系。
	先找b，若b与c没有关系，再继续向前找a，依次类推。
	（3）还支持省略中间表的功能，如 a join a_b join b 可以写成a join b。对于a join b 还是首先进行（2）的处理，当都找不到时，则判定通过中间表来联接
	（4）支持省略，中间表和附属表，如  product join product_apps join app，其中product_apps就是中间表，app join app_category join app_container
		中app_category、app_container为app的附属表
	
    
