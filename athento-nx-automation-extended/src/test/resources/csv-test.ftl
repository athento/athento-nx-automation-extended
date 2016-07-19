<#list headers as header>
<#t>${header}<#if header_has_next>,</#if>
</#list>

<#list This as doc>
<#list columns as col>
<#t>"${Func.getValue(doc, col)}"<#if col_has_next>,</#if>
</#list>
<#lt>

</#list>