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

public class Triple<A, B, C> {
    public final A fst;
    public final B snd;
    public final C trd;

    public Triple(A fst, B snd, C trd) {
        this.fst = fst;
        this.snd = snd;
        this.trd = trd;
    }

    @Override
    public int hashCode() {
        return (this.fst == null ? 0 : this.fst.hashCode()) ^ (this.snd == null ? 0 : this.snd.hashCode())
                ^ (this.trd == null ? 0 : this.trd.hashCode());
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        final Triple o = (Triple) obj;
        return (this.fst == null ? o.fst == null : this.fst.equals(o.fst))
                && (this.snd == null ? o.snd == null : this.snd.equals(o.snd))
                && (this.trd == null ? o.trd == null : this.trd.equals(o.trd));
    }
}
