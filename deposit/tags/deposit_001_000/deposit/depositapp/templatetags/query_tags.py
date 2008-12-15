from django import template

register = template.Library()

@register.tag
def querystring(parser, token):    
    split_contents = token.split_contents()
    args = []
    try:
        tag_name, query = split_contents[0], split_contents[1]
    except ValueError:
        raise template.TemplateSyntaxError, "query object must be passed as first argument"
    if len(split_contents) > 2:
        args = split_contents[2:]
    kw_args = {}
    for arg in args:
        try:
            name, value = arg.split("=")
        except ValueError:
            raise template.TemplateSyntaxError, "%s must be name=value" % arg
        kw_args[str(name)]=value
        
    return QuerystringNode(query, kw_args)
        
class QuerystringNode(template.Node):    
    def __init__(self, query, kw_args):
        self.query=template.Variable(query)
        self.kw_args = kw_args
    
    def render(self, context):
        try:
            query = self.query.resolve(context)
        except template.VariableDoesNotExist:
            return ''
        new_args = {}
        for name in self.kw_args.keys():
            value = self.kw_args[name]
            var = template.Variable(value)
            try:
                new_value = var.resolve(context)
            except template.VariableDoesNotExist:
                new_value = value
            new_args[name]=new_value
        return query.get_querystring(**new_args)
        