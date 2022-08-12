Managing state is hard

Method:

Declare the state atom, then return a map with the getters and setters from this state.
The following naming rules apply:
    setters always end in !, eg. `set-name!`
    getters are always reactions.

`(let [state (r/atom {:username ""})]
   {:set-username! #(swap! state assoc :username (-> % .-target .-value))
   })
