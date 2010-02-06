/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */
package org.openengsb.util.tuple;

public class Pair<A, B> {
    public final A fst;
    public final B snd;

    public Pair(A fst, B snd) {
        this.fst = fst;
        this.snd = snd;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !o.getClass().equals(getClass())) {
            return false;
        }
        Pair other = (Pair) o;
        return (this.fst == null ? other.fst == null : this.fst.equals(other.fst))
                && (this.snd == null ? other.snd == null : this.snd.equals(other.snd));
    }

    @Override
    public int hashCode() {
        return (this.fst == null ? 0 : this.fst.hashCode()) ^ (this.snd == null ? 1 : this.snd.hashCode());
    }
}
