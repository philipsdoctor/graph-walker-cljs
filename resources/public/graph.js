var graph = {
    "graph-name" : "Tier 1 Support",
    nodes : {
        foo : {
            value : "Call engineering",
            "choice-node-pairs" : null,
        },
        bar : {
            value : "Did you google the problem?",
            "choice-node-pairs" : [["Yes", "foo"]],
        },
        done : {
            value : "File a JIRA ticket and drop a note in Eng chat.",
            "choice-node-pairs" : null,
        },
        "not-sure" : {
            value : "Try to log into the website.",
            "choice-node-pairs" : null,
        },
        baz : {
            value : "Does this issue actually impact customers?",
            "choice-node-pairs" : [["Yes", "foo"], ["No", "done"], ["I don't know", "not-sure"]],
        },
    },
    "starting-node" : "baz",
}