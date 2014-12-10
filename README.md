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